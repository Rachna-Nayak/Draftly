import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getPaper, addSection, submitFeedback, approvePaper } from '../api';

export default function PaperView() {
  const { paperId } = useParams();
  const [paper, setPaper] = useState(null);
  const [sectionForm, setSectionForm] = useState({ sectionName: 'Abstract', content: '', order: 1 });
  const [feedbackForm, setFeedbackForm] = useState({ facultyId: '', facultyName: '', sectionName: '', comment: '', status: 'APPROVED' });

  const reload = () => getPaper(paperId).then((res) => setPaper(res.data));

  useEffect(() => { reload(); }, [paperId]);

  const handleAddSection = async (e) => {
    e.preventDefault();
    await addSection(paperId, sectionForm);
    setSectionForm({ sectionName: 'Abstract', content: '', order: 1 });
    reload();
  };

  const handleFeedback = async (e) => {
    e.preventDefault();
    await submitFeedback(paperId, feedbackForm);
    setFeedbackForm({ facultyId: '', facultyName: '', sectionName: '', comment: '', status: 'APPROVED' });
    reload();
  };

  const handleApprove = async () => {
    await approvePaper(paperId);
    reload();
  };

  if (!paper) return <p>Loading...</p>;

  return (
    <>
      <h1>{paper.title}</h1>
      <span className={`badge ${paper.status === 'APPROVED' ? 'badge-success' : 'badge-danger'}`}>{paper.status}</span>
      {paper.status !== 'APPROVED' && (
        <button className="btn btn-success" style={{ marginLeft: '1rem' }} onClick={handleApprove}>✅ Approve Paper</button>
      )}

      {/* Sections */}
      <h2 style={{ marginTop: '1.5rem' }}>Sections</h2>
      {paper.sections?.map((section, i) => (
        <div key={i} className="card">
          <h3>{section.sectionName}</h3>
          <p><em>Order: {section.order}</em></p>
          <div style={{ background: '#f8f9fa', padding: '1rem', borderRadius: '5px', whiteSpace: 'pre-wrap' }}>{section.content}</div>
        </div>
      ))}

      {/* Add Section Form */}
      <div className="card">
        <h3>Add New Section</h3>
        <form onSubmit={handleAddSection}>
          <div className="form-group">
            <label>Section Name</label>
            <select value={sectionForm.sectionName} onChange={(e) => setSectionForm({ ...sectionForm, sectionName: e.target.value })}>
              {['Abstract', 'Introduction', 'Literature Review', 'Methodology', 'Results', 'Discussion', 'Conclusion'].map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Content</label>
            <textarea value={sectionForm.content} onChange={(e) => setSectionForm({ ...sectionForm, content: e.target.value })} required placeholder="Write section content..." />
          </div>
          <div className="form-group">
            <label>Order</label>
            <input type="number" value={sectionForm.order} onChange={(e) => setSectionForm({ ...sectionForm, order: parseInt(e.target.value) })} min="1" />
          </div>
          <button type="submit" className="btn btn-primary">Add Section</button>
        </form>
      </div>

      {/* Feedback */}
      <h2>Feedback</h2>
      {(!paper.feedbacks || paper.feedbacks.length === 0) && <p>No feedback yet.</p>}
      {paper.feedbacks?.map((fb, i) => (
        <div key={i} className="feedback-item">
          <p><strong>{fb.facultyName}</strong> on <em>{fb.sectionName}</em></p>
          <p>{fb.comment}</p>
          <span className={`badge ${fb.status === 'APPROVED' ? 'badge-success' : 'badge-danger'}`}>{fb.status}</span>
          <p style={{ fontSize: '0.8rem', color: '#999' }}>{fb.createdAt}</p>
        </div>
      ))}

      {/* Feedback Form */}
      <div className="card" style={{ marginTop: '1rem' }}>
        <h3>💬 Submit Feedback</h3>
        <form onSubmit={handleFeedback}>
          <div className="form-group">
            <label>Faculty ID</label>
            <input value={feedbackForm.facultyId} onChange={(e) => setFeedbackForm({ ...feedbackForm, facultyId: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>Faculty Name</label>
            <input value={feedbackForm.facultyName} onChange={(e) => setFeedbackForm({ ...feedbackForm, facultyName: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>Section</label>
            <input value={feedbackForm.sectionName} onChange={(e) => setFeedbackForm({ ...feedbackForm, sectionName: e.target.value })} required placeholder="e.g., Methodology" />
          </div>
          <div className="form-group">
            <label>Comment</label>
            <textarea value={feedbackForm.comment} onChange={(e) => setFeedbackForm({ ...feedbackForm, comment: e.target.value })} required placeholder="Your feedback..." />
          </div>
          <div className="form-group">
            <label>Status</label>
            <select value={feedbackForm.status} onChange={(e) => setFeedbackForm({ ...feedbackForm, status: e.target.value })}>
              <option value="APPROVED">Approved</option>
              <option value="REVISION_NEEDED">Revision Needed</option>
            </select>
          </div>
          <button type="submit" className="btn btn-success">Submit Feedback</button>
        </form>
      </div>
    </>
  );
}
