// pages/ApplicantDetail.jsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FileText, Filter, FileWarning, Award, Target, ChevronLeft, ChevronRight } from 'lucide-react';
import '../styles/ApplicantDetail.css';

export default function ApplicantDetail({
  applicants,
  file,
  filterCategory,
  onFilterCategory
}) {
  const { id } = useParams(); // URL에서 id 가져오기
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  // ID로 지원자 찾기
  const selectedApplicant = applicants.find(
    (applicant) => applicant.id === parseInt(id)
  );

  // skills 기본값
  const skills = selectedApplicant?.skills || {
    required: [],
    preferred: [],
    tools: []
  };

  // 페이지 로드 시 스크롤 최상단으로
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [id]);

  // 지원자를 찾지 못한 경우
  if (!selectedApplicant) {
    return (
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
    );
  }

  // 다른 지원자 클릭 시
  const handleApplicantClick = (applicant) => {
    navigate(`/applicant/${applicant.id}`);
  };

  // 점수순으로 정렬 (높은 순)
  const sortedApplicants = [...applicants].sort((a, b) => b.score - a.score);

  // 카테고리 필터링
  const filteredApplicants = filterCategory === '전체'
    ? sortedApplicants
    : sortedApplicants.filter(applicant => applicant.category === filterCategory);

  // 카테고리별 개수 계산
  const categoryCounts = {
    '전체': applicants.length,
    '프론트엔드': applicants.filter(a => a.category === '프론트엔드').length,
    '백엔드': applicants.filter(a => a.category === '백엔드').length,
    '기획자': applicants.filter(a => a.category === '기획자').length
  };

  // PDF URL 생성
  const pdfUrl = file ? URL.createObjectURL(file) : null;

  return (
    <div className="app-container">
      {/* Header */}
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
        {/* Sidebar */}
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

            {/* 직무 필터 섹션 */}
            <div className="filter-section">
              <div className="filter-header">
                <Filter className="filter-icon" />
                <span className="filter-label">직무 필터</span>
              </div>
              <div className="filter-buttons">
                {['전체', '프론트엔드', '백엔드', '기획자'].map((category) => (
                  <button
                    key={category}
                    className={`filter-button ${filterCategory === category ? 'active' : ''}`}
                    onClick={() => onFilterCategory(category)}
                  >
                    {category}
                    <span className="filter-count">({categoryCounts[category]})</span>
                  </button>
                ))}
              </div>
            </div>
            
            {/* 필터링된 지원자 목록 */}
            <div className="applicant-list">
              {filteredApplicants.map((applicant) => (
                <div
                  key={applicant.id}
                  className={`applicant-card ${selectedApplicant.id === applicant.id ? 'active' : ''}`}
                  onClick={() => handleApplicantClick(applicant)}
                >
                  <div className="applicant-header">
                    <span className="applicant-name">
                      {applicant.name}
                    </span>
                    <span className="category-badge">{applicant.category}</span>
                  </div>
                  <div className="applicant-score">{applicant.score}점</div>
                </div>
              ))}
            </div>
          </div>
        </aside>

        {/* Main Content - 지원자 상세 정보 */}
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
            {/* 왼쪽: PDF 뷰어 + 점수/분석 */}
            <div className="pdf-viewer-section">
              {/* PDF 뷰어 */}
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

              {/* 점수 및 분석 결과 */}
              <div className="analysis-card">
                <div className="score-section">
                  <h3 className="score-title">AI 매칭 점수</h3>
                  <div className="score-value">
                    <span className="score-number">{selectedApplicant.score}</span>
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
                    {selectedApplicant.analysis || '이력서에 기반한 분석 결과가 여기에 표시됩니다.'}
                  </p>
                </div>

                <div className="badge-section">
                  <Award className="badge-icon" />
                  <div className="badge-text">
                    <p className="badge-title">강점 요약</p>
                    <p className="badge-description">
                      {selectedApplicant.strengths || '주요 강점 및 차별화 포인트가 여기에 표시됩니다.'}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* 오른쪽: 매칭 키워드 */}
            <div className="keywords-sidebar">
              <div className="keywords-card">
                <h3 className="keywords-card-title">매칭 키워드</h3>
                
                <div className="keywords-section">
                  <div className="keywords-group">
                    <h4 className="keywords-group-title">필수 기술</h4>
                    <div className="keywords">
                      {skills.required.map((skill, idx) => (
                        <span key={idx} className="keyword-tag keyword-required">{skill}</span>
                      ))}
                    </div>
                  </div>

                  <div className="keywords-group">
                    <h4 className="keywords-group-title">우대 역량</h4>
                    <div className="keywords">
                      {skills.preferred.map((skill, idx) => (
                        <span key={idx} className="keyword-tag keyword-preferred">{skill}</span>
                      ))}
                    </div>
                  </div>

                  <div className="keywords-group">
                    <h4 className="keywords-group-title">사용 도구</h4>
                    <div className="keywords">
                      {skills.tools.map((tool, idx) => (
                        <span key={idx} className="keyword-tag keyword-tools">{tool}</span>
                      ))}
                    </div>
                  </div>
                </div>

                {/* 간단한 요약 정보 */}
                <div className="info-summary">
                  <h4 className="info-summary-title">지원자 정보</h4>
                  <div className="info-summary-grid">
                    <div className="info-summary-item">
                      <span className="info-summary-label">이름</span>
                      <span className="info-summary-value">{selectedApplicant.name}</span>
                    </div>
                    <div className="info-summary-item">
                      <span className="info-summary-label">직무</span>
                      <span className="info-summary-value">{selectedApplicant.category}</span>
                    </div>
                    <div className="info-summary-item">
                      <span className="info-summary-label">경력</span>
                      <span className="info-summary-value">
                        {selectedApplicant.career || '신입'}
                      </span>
                    </div>
                    <div className="info-summary-item">
                      <span className="info-summary-label">지역</span>
                      <span className="info-summary-value">{selectedApplicant.location}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
