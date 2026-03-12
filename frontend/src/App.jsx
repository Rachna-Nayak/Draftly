import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
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
import './App.css';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          {/* Home */}
          <Route path="/" element={<Home />} />

          {/* UC1: Projects */}
          <Route path="/projects" element={<ProjectList />} />
          <Route path="/projects/new" element={<ProjectCreate />} />
          <Route path="/projects/:id" element={<ProjectView />} />

          {/* UC2: Search  |  UC3: Credibility */}
          <Route path="/search" element={<Search />} />
          <Route path="/search/credibility/:paperId" element={<Credibility />} />

          {/* UC4: References */}
          <Route path="/projects/:projectId/references" element={<ReferenceList />} />
          <Route path="/projects/:projectId/references/suggestions" element={<ReferenceSuggestions />} />

          {/* UC6: Papers & Feedback */}
          <Route path="/projects/:projectId/papers" element={<PaperList />} />
          <Route path="/projects/:projectId/papers/new" element={<PaperCreate />} />
          <Route path="/papers/:paperId" element={<PaperView />} />

          {/* UC7: Plagiarism */}
          <Route path="/papers/:paperId/plagiarism" element={<PlagiarismReport />} />

          {/* UC8: Export */}
          <Route path="/papers/:paperId/export" element={<ExportReadiness />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
