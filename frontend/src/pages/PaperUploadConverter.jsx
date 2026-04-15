import { useState } from 'react';
import api from '../api';
import './PaperUploadConverter.css';

export default function PaperUploadConverter() {
  const [file, setFile] = useState(null);
  const [latex, setLatex] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleFileChange = (e) => {
    setFile(e.target.files?.[0] || null);
    setLatex('');
    setError('');
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Please select a .docx file first.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      setLoading(true);
      setError('');
      setLatex('');

      // DO NOT manually set Content-Type for FormData
      // Let axios set it with the correct multipart boundary
      const response = await api.post('/convert/docx-to-latex', formData);

      setLatex(response?.data?.latex || '');
    } catch (err) {
      console.error('Upload error:', err);
      console.error('Status:', err?.response?.status);
      
      if (err?.response?.status === 401) {
        setError('Session expired. Redirecting to login...');
        setTimeout(() => window.location.href = '/login', 2000);
        return;
      }
      
      setError(
        err?.response?.data?.message ||
          'Conversion failed. Please try again or check the file.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = () => {
    if (!latex) return;
    const blob = new Blob([latex], { type: 'text/x-tex;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = (file?.name || 'paper')?.replace(/\.docx?$/i, '') + '.tex';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="page-container">
      <h1>DOCX → LaTeX Converter</h1>
      <p className="page-description">
        Upload a Word (.docx) file to convert it to LaTeX, preview the result, and download the
        generated .tex file.
      </p>

      <form className="upload-form" onSubmit={handleUpload}>
        <div className="form-row">
          <input
            type="file"
            accept=".docx"
            onChange={handleFileChange}
            disabled={loading}
          />
          <button type="submit" disabled={loading || !file}>
            {loading ? 'Converting…' : 'Convert to LaTeX'}
          </button>
        </div>
        {error && <div className="error-message">{error}</div>}
      </form>

      {latex && (
        <div className="latex-output-section">
          <div className="latex-output-header">
            <h2>LaTeX Output</h2>
            <button onClick={handleDownload}>Download .tex</button>
          </div>
          <pre className="latex-output">
            <code>{latex}</code>
          </pre>
        </div>
      )}
    </div>
  );
}
