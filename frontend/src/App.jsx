import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Unauthorized from './pages/Unauthorized';
import ProjectList from './pages/ProjectList';
import ProjectCreate from './pages/ProjectCreate';
import ProjectView from './pages/ProjectView';
import Search from './pages/Search';
import Credibility from './pages/Credibility';
import ReferenceList from './pages/ReferenceList';
import ReferenceSuggestions from './pages/ReferenceSuggestions';
import PaperList from './pages/PaperList';
import PaperCreate from './pages/PaperCreate';
import PaperView from './pages/PaperView';
import PlagiarismReport from './pages/PlagiarismReport';
import ExportReadiness from './pages/ExportReadiness';
import MetricsDashboard from './pages/MetricsDashboard';
import AnalyticsPage from './pages/AnalyticsPage';
import NotificationCenter from './pages/NotificationCenter';
import ReviewQueue from './pages/ReviewQueue';
import SubmissionList from './pages/SubmissionList';
import SubmissionCreate from './pages/SubmissionCreate';
import ReviewerAssignment from './pages/ReviewerAssignment';
import PaperUploadConverter from './pages/PaperUploadConverter';
import './components/Layout.css';
import './App.css';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            {/* Home - Available to all authenticated users */}
            <Route path="/" element={<Home />} />

            <Route element={<ProtectedRoute roles={['AUTHOR', 'REVIEWER', 'ADMIN']} />}>
              {/* UC1: Projects */}
              <Route path="/projects" element={<ProjectList />} />
              <Route path="/projects/:id" element={<ProjectView />} />

              {/* UC2: Search  |  UC3: Credibility */}
              <Route path="/search" element={<Search />} />
              <Route path="/search/credibility/:paperId" element={<Credibility />} />

              {/* UC4: References */}
              <Route path="/projects/:projectId/references" element={<ReferenceList />} />
              <Route path="/projects/:projectId/references/suggestions" element={<ReferenceSuggestions />} />

              {/* UC6: Papers */}
              <Route path="/projects/:projectId/papers" element={<PaperList />} />
              <Route path="/papers/:paperId" element={<PaperView />} />

              {/* DOCX → LaTeX Converter */}
              <Route path="/converter" element={<PaperUploadConverter />} />

              {/* Submissions */}
              <Route path="/submissions" element={<SubmissionList />} />

              {/* UC7: Plagiarism */}
              <Route path="/papers/:paperId/plagiarism" element={<PlagiarismReport />} />

              {/* UC8: Export */}
              <Route path="/papers/:paperId/export" element={<ExportReadiness />} />

              {/* Metrics */}
              <Route path="/metrics" element={<MetricsDashboard />} />
            </Route>

            <Route element={<ProtectedRoute roles={['AUTHOR', 'ADMIN']} />}>
              <Route path="/projects/new" element={<ProjectCreate />} />
              <Route path="/projects/:projectId/papers/new" element={<PaperCreate />} />
              <Route path="/submissions/new" element={<SubmissionCreate />} />
            </Route>

            <Route element={<ProtectedRoute roles={['REVIEWER', 'ADMIN']} />}>
              <Route path="/notifications" element={<NotificationCenter />} />
              <Route path="/review-queue" element={<ReviewQueue />} />
            </Route>

            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path="/reviewer-assignment" element={<ReviewerAssignment />} />
              <Route path="/analytics" element={<AnalyticsPage />} />
            </Route>
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
