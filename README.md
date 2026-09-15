# Crop Disease Detector 🌾

An AI-powered web application that detects crop diseases from images using deep learning and computer vision. Upload a photo of your crop, and get instant disease identification with treatment recommendations.

## Features

- **Image Upload & Analysis**: Upload crop images for real-time disease detection
- **Disease Classification**: Identifies 38+ crop diseases across multiple plant types
- **Severity Assessment**: Rates disease severity (Mild, Moderate, Severe)
- **Treatment Recommendations**: Provides actionable treatment suggestions
- **Disease History**: Stores detection history for tracking and analysis
- **Multi-Crop Support**: Supports wheat, potato, tomato, corn, rice, and more
- **Responsive UI**: Works seamlessly on desktop and mobile devices

## Tech Stack

**Frontend:**
- React 18 with TypeScript
- Next.js (App Router)
- TailwindCSS for styling
- Axios for API calls

**Backend:**
- Python 3.11
- FastAPI for REST API
- TensorFlow/Keras for deep learning
- Pillow for image processing
- SQLAlchemy with SQLite/PostgreSQL

**ML Model:**
- Pre-trained ResNet-50 CNN
- Trained on PlantVillage dataset
- Transfer learning for accurate classification

## Project Structure

```
crop-disease-detector/
├── frontend/                 # React/Next.js application
│   ├── app/                 # Next.js app router
│   ├── components/          # React components
│   ├── lib/                 # Utilities and helpers
│   ├── public/              # Static assets
│   └── package.json
│
├── backend/                 # FastAPI application
│   ├── app/
│   │   ├── main.py         # FastAPI app entry point
│   │   ├── api/            # API routes
│   │   ├── models/         # Database models
│   │   ├── schemas/        # Pydantic schemas
│   │   ├── services/       # Business logic
│   │   └── ml/             # ML inference code
│   ├── models/             # Pre-trained ML models
│   ├── requirements.txt
│   └── .env.example
│
├── docs/                    # Documentation
├── docker-compose.yml       # Docker orchestration
└── .github/workflows/       # CI/CD pipelines
```

## Quick Start

### Prerequisites
- Node.js 18+
- Python 3.11+
- pip and npm/yarn

### Backend Setup

```bash
# Navigate to backend directory
cd backend

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Set up environment variables
cp .env.example .env

# Download pre-trained model
python scripts/download_model.py

# Run the server
uvicorn app.main:app --reload --port 8000
```

### Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Set up environment variables
echo "NEXT_PUBLIC_API_URL=http://localhost:8000" > .env.local

# Run development server
npm run dev
```

The app will be available at `http://localhost:3000`

## API Endpoints

### Disease Detection
- **POST** `/api/detect` - Detect disease from image
  - Request: multipart/form-data with image file
  - Response: Disease name, confidence, severity, treatment

### Detection History
- **GET** `/api/history` - Get all detections for current user
- **GET** `/api/history/{id}` - Get specific detection details
- **DELETE** `/api/history/{id}` - Delete detection record

### Crop Information
- **GET** `/api/crops` - List all supported crops
- **GET** `/api/crops/{crop_id}/diseases` - Get diseases for a specific crop

## Model Performance

- **Accuracy**: 94.7% on validation set
- **Supported Diseases**: 38+ diseases across 11 crop types
- **Inference Time**: ~200ms per image (GPU), ~800ms (CPU)

## Supported Crops & Diseases

### Tomato
- Early Blight
- Late Blight
- Leaf Mold
- Septoria Leaf Spot
- Spider Mites
- Target Spot
- Healthy

### Potato
- Early Blight
- Late Blight
- Healthy

### Corn
- Gray Leaf Spot
- Common Rust
- Northern Leaf Blight
- Healthy

### Wheat
- Septoria
- Stripe Rust
- Healthy

[See full disease list in docs/DISEASES.md]

## Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up --build

# Frontend: http://localhost:3000
# Backend: http://localhost:8000
# API Docs: http://localhost:8000/docs
```

## Environment Variables

### Backend (.env)
```
DATABASE_URL=sqlite:///./test.db
JWT_SECRET_KEY=your-secret-key
ML_MODEL_PATH=./models/disease_detector.h5
UPLOAD_FOLDER=./uploads
MAX_UPLOAD_SIZE=10485760  # 10MB
```

### Frontend (.env.local)
```
NEXT_PUBLIC_API_URL=http://localhost:8000
NEXT_PUBLIC_APP_NAME=Crop Disease Detector
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Development Roadmap

- [ ] Mobile app (React Native)
- [ ] Batch processing for multiple images
- [ ] Real-time webcam detection
- [ ] Disease severity level tracking
- [ ] Treatment effectiveness feedback
- [ ] Multi-language support
- [ ] Weather-based disease prediction
- [ ] Integration with agricultural databases

## Performance Tips

- Use GPU for faster inference (set `CUDA_VISIBLE_DEVICES`)
- Compress images before upload (recommended: < 2MB)
- Implement image caching on frontend
- Use database indexing for large history datasets

## Troubleshooting

**Model not found error**
```bash
cd backend
python scripts/download_model.py
```

**CORS errors**
- Ensure `ALLOWED_ORIGINS` in backend .env includes frontend URL
- Check if both services are running on correct ports

**Image upload fails**
- Check MAX_UPLOAD_SIZE environment variable
- Verify upload folder has write permissions
- Ensure image format is supported (JPG, PNG, WebP)

## License

MIT License - see LICENSE file for details

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing documentation in `/docs`
- Review FAQ in docs/FAQ.md

## Acknowledgments

- PlantVillage dataset for training data
- FastAPI for the backend framework
- Next.js team for the frontend framework
- TensorFlow community for ML tools