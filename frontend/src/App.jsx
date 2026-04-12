import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import RoleRoute from './components/RoleRoute';
import { USER_ROLES } from './roleAccess';
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

            {/* UC2: Search - All authenticated users */}
            <Route path="/search" element={<Search />} />
            <Route path="/search/credibility/:paperId" element={<Credibility />} />

            {/* Submissions - All authenticated users */}
            <Route path="/submissions" element={<SubmissionList />} />
            <Route path="/submissions/new" element={<SubmissionCreate />} />

            {/* UC1: Projects - Authors only */}
            <Route 
              path="/projects"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><ProjectList /></RoleRoute>}
            />
            <Route 
              path="/projects/new"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><ProjectCreate /></RoleRoute>}
            />
            <Route 
              path="/projects/:id"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><ProjectView /></RoleRoute>}
            />

            {/* UC4: References - Authors only */}
            <Route 
              path="/projects/:projectId/references"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><ReferenceList /></RoleRoute>}
            />
            <Route 
              path="/projects/:projectId/references/suggestions"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><ReferenceSuggestions /></RoleRoute>}
            />

            {/* UC6: Papers & Feedback - Authors + Reviewers */}
            <Route 
              path="/projects/:projectId/papers"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR, USER_ROLES.REVIEWER]}><PaperList /></RoleRoute>}
            />
            <Route 
              path="/projects/:projectId/papers/new"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><PaperCreate /></RoleRoute>}
            />
            <Route 
              path="/papers/:paperId"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR, USER_ROLES.REVIEWER]}><PaperView /></RoleRoute>}
            />

            {/* UC7: Plagiarism - Authors only */}
            <Route 
              path="/papers/:paperId/plagiarism"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><PlagiarismReport /></RoleRoute>}
            />

            {/* UC8: Export - Authors only */}
            <Route 
              path="/papers/:paperId/export"
              element={<RoleRoute allowedRoles={[USER_ROLES.AUTHOR]}><ExportReadiness /></RoleRoute>}
            />

            {/* Review Queue - Reviewers + Admins only */}
            <Route 
              path="/review-queue"
              element={<RoleRoute allowedRoles={[USER_ROLES.REVIEWER, USER_ROLES.ADMIN]}><ReviewQueue /></RoleRoute>}
            />

            {/* Reviewer Assignment - Admins only */}
            <Route 
              path="/reviewer-assignment"
              element={<RoleRoute allowedRoles={[USER_ROLES.ADMIN]}><ReviewerAssignment /></RoleRoute>}
            />

            {/* Metrics & Analytics - Admins only */}
            <Route 
              path="/metrics"
              element={<RoleRoute allowedRoles={[USER_ROLES.ADMIN]}><MetricsDashboard /></RoleRoute>}
            />
            <Route 
              path="/analytics"
              element={<RoleRoute allowedRoles={[USER_ROLES.ADMIN]}><AnalyticsPage /></RoleRoute>}
            />
            <Route 
              path="/notifications"
              element={<RoleRoute allowedRoles={[USER_ROLES.ADMIN]}><NotificationCenter /></RoleRoute>}
            />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
