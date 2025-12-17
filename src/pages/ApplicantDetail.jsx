// pages/ApplicantDetail.jsx
import React, { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FileText, Filter, FileWarning, Target, ChevronLeft, ChevronRight } from 'lucide-react';
import '../styles/ApplicantDetail.css';

export default function ApplicantDetail({
  applicants,
  file,
  filterCategory,
  onFilterCategory
}) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const selectedApplicant = applicants.find(
    (applicant) => applicant.id === parseInt(id)
  );

  const getDisplayName = (applicant) => {
    if (applicant.name) return applicant.name;
    if (applicant.pdfName) return applicant.pdfName.replace(/\.[^/.]+$/, '');
    return `지원자 ${applicant.id}`;
  };

  const sortedApplicants = useMemo(() => {
    return [...applicants].sort((a, b) => (b.score ?? 0) - (a.score ?? 0));
  }, [applicants]);

  const roleOptions = useMemo(() => {
    const roles = new Set();
    applicants.forEach((a) => {
      if (a?.role && String(a.role).trim() !== '') roles.add(a.role);
    });
    return ['전체', ...Array.from(roles)];
  }, [applicants]);

  const filteredApplicants = useMemo(() => {
    if (filterCategory === '전체') return sortedApplicants;
    return sortedApplicants.filter((a) => a.role === filterCategory);
  }, [sortedApplicants, filterCategory]);

  const roleCounts = useMemo(() => {
    const counts = { '전체': applicants.length };
    roleOptions.forEach((r) => {
      if (r !== '전체') counts[r] = applicants.filter((a) => a.role === r).length;
    });
    return counts;
  }, [applicants, roleOptions]);

  const requiredSkills = useMemo(() => {
    const raw = selectedApplicant?.keyword;
    if (!raw) return [];
    return String(raw)
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean)
      .filter((v, i, arr) => arr.indexOf(v) === i);
  }, [selectedApplicant?.keyword]);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [id]);

  if (!selectedApplicant) {
    return (
      <div className="app-zoom-wrapper">
        <div className="app-zoom-content">
          <div className="app-container">
            <header className="header">
              <div className="header-content">
                <FileText className="header-icon" />
                <h1
                  className="header-title"
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate('/')}
                >
                  ResuMatch
                </h1>
              </div>
            </header>
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              height: 'calc(100vh - 5rem)',
              gap: '1rem'
            }}>
              <h2 style={{ color: '#2E2E2E' }}>지원자를 찾을 수 없습니다</h2>
              <button
                onClick={() => navigate('/')}
                style={{
                  padding: '0.75rem 2rem',
                  backgroundColor: '#8AA399',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontSize: '1rem',
                  fontWeight: '500'
                }}
              >
                메인으로 돌아가기
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const handleApplicantClick = (applicant) => {
    navigate(`/applicant/${applicant.id}`);
  };

  const pdfUrl = file ? URL.createObjectURL(file) : null;

  return (
    <div className="app-zoom-wrapper">
      <div className="app-zoom-content">
        <div className="app-container">
          <header className="header">
            <div className="header-content">
              <FileText className="header-icon" />
              <h1
                className="header-title"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate('/')}
              >
                ResuMatch
              </h1>
            </div>
          </header>

          <div className="main-layout">
            <aside className={`sidebar ${isSidebarOpen ? 'open' : 'closed'}`}>
              <button
                className="sidebar-toggle"
                onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                aria-label={isSidebarOpen ? '사이드바 접기' : '사이드바 펼치기'}
              >
                {isSidebarOpen ? <ChevronLeft size={20} /> : <ChevronRight size={20} />}
              </button>

              <div className="sidebar-content">
                <div className="sidebar-header">
                  <h2 className="sidebar-title">지원자 목록</h2>
                  <span className="applicant-count">{filteredApplicants.length}명</span>
                </div>

                <div className="filter-section">
                  <div className="filter-header">
                    <Filter className="filter-icon" />
                    <span className="filter-label">직무 필터</span>
                  </div>

                  <div className="filter-buttons">
                    {roleOptions.map((role) => (
                      <button
                        key={role}
                        className={`filter-button ${filterCategory === role ? 'active' : ''}`}
                        onClick={() => onFilterCategory(role)}
                      >
                        {role}
                        <span className="filter-count">({roleCounts[role] ?? 0})</span>
                      </button>
                    ))}
                  </div>
                </div>

                <div className="applicant-list">
                  {filteredApplicants.map((applicant) => (
                    <div
                      key={applicant.id}
                      className={`applicant-card ${selectedApplicant.id === applicant.id ? 'active' : ''}`}
                      onClick={() => handleApplicantClick(applicant)}
                    >
                      <div className="applicant-header">
                        <span className="applicant-name">
                          {getDisplayName(applicant)}
                        </span>
                        <span className="category-badge">{applicant.role || '미지정'}</span>
                      </div>
                      <div className="applicant-score">{applicant.score ?? 0}점</div>
                    </div>
                  ))}
                </div>
              </div>
            </aside>

            <main className="main-content detail-view">
              <div className="detail-header">
                <h2 className="detail-title">이력서 내용</h2>
                <button
                  className="close-button"
                  onClick={() => navigate('/')}
                >
                  ✕
                </button>
              </div>

              <div className="detail-container">
                <div className="pdf-viewer-section">
                  {pdfUrl ? (
                    <div className="pdf-viewer-container">
                      <iframe
                        src={pdfUrl}
                        className="pdf-viewer"
                        title="이력서 PDF"
                      />
                    </div>
                  ) : (
                    <div className="no-pdf-message">
                      <FileWarning className="no-pdf-icon" />
                      <p className="no-pdf-text">업로드된 이력서가 없습니다</p>
                      <p className="no-pdf-subtext">메인 페이지에서 이력서를 업로드해주세요</p>
                    </div>
                  )}

                  <div className="analysis-card">
                    <div className="score-section">
                      <h3 className="score-title">AI 매칭 점수</h3>
                      <div className="score-value">
                        <span className="score-number">{selectedApplicant.score ?? 0}</span>
                        <span className="score-unit"> / 100</span>
                      </div>
                      <p className="score-description">
                        해당 직무와의 적합도를 점수로 환산한 값입니다.
                      </p>
                    </div>

                    <div className="analysis-section">
                      <h3 className="analysis-title">
                        <Target className="analysis-icon" />
                        AI 분석 결과
                      </h3>
                      <p className="analysis-text">
                        {selectedApplicant.analysis || selectedApplicant.aiSummary || '분석 결과가 없습니다.'}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="keywords-sidebar">
                  <div className="keywords-card">
                    <h3 className="keywords-card-title">매칭 키워드</h3>

                    <div className="keywords-section">
                      <div className="keywords-group">
                        <h4 className="keywords-group-title">필수 기술</h4>
                        <div className="keywords">
                          {requiredSkills.length > 0 ? (
                            requiredSkills.map((skill, idx) => (
                              <span key={idx} className="keyword-tag keyword-required">{skill}</span>
                            ))
                          ) : (
                            <span className="keyword-empty">키워드가 없습니다</span>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="info-summary">
                      <h4 className="info-summary-title">지원자 정보</h4>
                      <div className="info-summary-grid">
                        <div className="info-summary-item">
                          <span className="info-summary-label">이름</span>
                          <span className="info-summary-value">
                            {getDisplayName(selectedApplicant)}
                          </span>
                        </div>
                        <div className="info-summary-item">
                          <span className="info-summary-label">직무</span>
                          <span className="info-summary-value">
                            {selectedApplicant.role || '미지정'}
                          </span>
                        </div>
                      </div>
                    </div>

                  </div>
                </div>
              </div>
            </main>
          </div>
        </div>
      </div>
    </div>
  );
}
