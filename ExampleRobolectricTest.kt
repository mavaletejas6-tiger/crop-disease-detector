package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.model.CropTypeRepository
import com.example.model.ReferenceScale
import com.example.service.ImageProcessingHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GrainScan", appName)
  }

  @Test
  fun `verify reference scale calculations for 1 Euro coin`() {
    val scale = ReferenceScale(
      presetId = "coin_one_euro",
      realMm = 23.25f,
      pixelSpan = 135f
    )
    val ppm = scale.pixelsPerMm
    // 135 px / 23.25 mm approx 5.806 px/mm
    assertTrue(ppm > 5.7f && ppm < 5.9f)

    val measuredMm = scale.pixelsToMm(135f)
    assertEquals(23.25f, measuredMm, 0.05f)

    val pixelsFromMm = scale.mmToPixels(10f)
    assertEquals(58.06f, pixelsFromMm, 0.5f)
    assertTrue(scale.isCircular)
  }

  @Test
  fun `verify auto detect scale object on image helper`() {
    val testBitmap = Bitmap.createBitmap(480, 360, Bitmap.Config.ARGB_8888)
    val detection = ImageProcessingHelper.autoDetectScaleObjectOnImage(
      bitmap = testBitmap,
      canvasWidth = 480f,
      canvasHeight = 360f,
      isCircular = true,
      referenceMm = 23.25f
    )

    assertNotNull(detection)
    assertTrue(detection.spanPx > 0f)
    assertTrue(detection.confidence > 0f)
    assertTrue(detection.centerX > 0f && detection.centerX < 480f)
  }

  @Test
  fun `verify segment grains on image`() {
    val wheat = CropTypeRepository.crops.first { it.id.startsWith("wheat") }
    val ppm = 5.8f
    val grains = ImageProcessingHelper.segmentGrainsOnImage(
      crop = wheat,
      pixelsPerMm = ppm,
      canvasWidth = 480f,
      canvasHeight = 360f,
      scaleCenterX = 140f,
      scaleCenterY = 145f
    )

    assertTrue(grains.isNotEmpty())
    grains.forEach { grain ->
      val lengthMm = grain.lengthMm(ppm)
      val widthMm = grain.widthMm(ppm)
      assertTrue("Length should be positive", lengthMm > 2.0f)
      assertTrue("Width should be positive", widthMm > 1.0f)
      assertTrue("Length should be greater than width", lengthMm > widthMm)
    }
  }

  @Test
  fun `verify disease specialist consultation response`() = kotlinx.coroutines.runBlocking {
    val service = com.example.service.GeminiAgronomistService()
    val blast = com.example.model.DiseasePestCatalog.items.first { it.id == "rice_blast" }
    val response = service.consultDiseaseSpecialist(
      userQuestion = "How do I treat this disease with fungicide?",
      cropName = "Paddy Rice",
      diseaseItem = blast,
      diseaseScaleLevel = 2,
      damagePercentage = 12.0f,
      weatherTempC = 24.5,
      weatherHumidity = 78
    )

    assertTrue(response.isNotBlank())
    assertTrue(response.contains("Treatment", ignoreCase = true) || response.contains("Chemical", ignoreCase = true) || response.contains("Tricyclazole", ignoreCase = true))
  }

  @Test
  fun `verify live weather globe data and calculations`() {
    val data = com.example.model.WeatherOverlayData(
      temperatureC = 3.5,
      relativeHumidityPercent = 75,
      rainfallMm = 4.2,
      latitude = 48.8566,
      longitude = 2.3522,
      cityName = "Paris, France"
    )

    assertEquals("Paris, France", data.cityName)
    assertTrue("Should indicate North latitude", data.formattedLatitude.contains("N"))
    assertTrue("Should indicate East longitude", data.formattedLongitude.contains("E"))
    assertEquals(4.2, data.rainfallMm, 0.01)
    assertEquals(75, data.relativeHumidityPercent)
    assertEquals(3.5, data.temperatureC, 0.01)
    assertTrue("Coldness should indicate high cold / frost alert", data.coldnessLevel.contains("Coldness") || data.coldnessLevel.contains("Frost"))

    val service = com.example.service.WeatherService()
    val city = service.lookupNearestCity(48.8566, 2.3522)
    assertTrue("Nearest city should resolve to Paris", city.contains("Paris"))
  }

  @Test
  fun `verify satellite globe worldwide cities list and search`() {
    val cities = com.example.ui.components.ALL_WORLD_CITIES
    assertTrue("Should have extensive list of worldwide cities", cities.size >= 60)

    val paris = cities.find { it.name == "Paris" }
    assertNotNull(paris)
    assertEquals("France", paris?.country)
    assertEquals("Europe", paris?.continent)

    val tokyo = cities.find { it.name == "Tokyo" }
    assertNotNull(tokyo)
    assertEquals("Japan", tokyo?.country)

    val desMoines = cities.find { it.name == "Des Moines" }
    assertNotNull(desMoines)
    assertEquals("USA", desMoines?.country)

    // Search filter check
    val filtered = cities.filter { it.name.contains("Des", ignoreCase = true) || it.country.contains("USA", ignoreCase = true) }
    assertTrue(filtered.isNotEmpty())
  }

  @Test
  fun `verify document hub catalog, categories, and inspection certificate generation`() {
    val defaultDocs = com.example.model.DocumentCatalog.getDefaultDocuments()
    assertTrue("Document catalog should contain preset agronomy & grading standards", defaultDocs.size >= 4)

    val usdaDoc = defaultDocs.find { it.category == com.example.model.DocumentCategory.GRADING_STANDARDS }
    assertNotNull(usdaDoc)
    assertTrue(usdaDoc!!.fullContent.contains("USDA & FAO GRAIN GRADING"))

    val storageDoc = defaultDocs.find { it.category == com.example.model.DocumentCategory.STORAGE_PROTOCOLS }
    assertNotNull(storageDoc)
    assertTrue(storageDoc!!.fullContent.contains("AERATION"))

    val ipmDoc = defaultDocs.find { it.category == com.example.model.DocumentCategory.AGRONOMY_GUIDES }
    assertNotNull(ipmDoc)
    assertTrue(ipmDoc!!.fullContent.contains("INTEGRATED PEST MANAGEMENT"))

    // Test creating custom field note
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.viewmodel.GrainScanViewModel(context)
    val initialDocCount = vm.uiState.value.documents.size

    vm.addCustomDocument(
      title = "Plot 3 Post-Emergence Check",
      category = com.example.model.DocumentCategory.CUSTOM_NOTES,
      content = "High tillering observed with uniform 14cm plant height.",
      author = "Agronomist Jane"
    )

    assertEquals(initialDocCount + 1, vm.uiState.value.documents.size)
    assertEquals("Plot 3 Post-Emergence Check", vm.uiState.value.documents.first().title)
    assertTrue(vm.uiState.value.isViewingDocument)

    // Test generating active inspection certificate
    vm.generateInspectionCertificateFromScan()
    val cert = vm.uiState.value.documents.first()
    assertTrue(cert.isOfficialCertificate)
    assertTrue(cert.fullContent.contains("OFFICIAL GRAIN QUALITY INSPECTION CERTIFICATE"))
    assertTrue(cert.fullContent.contains("Kernel Length"))
    assertTrue(cert.fullContent.contains("AGRONOMIC CONFORMITY VERDICT"))
  }
}


