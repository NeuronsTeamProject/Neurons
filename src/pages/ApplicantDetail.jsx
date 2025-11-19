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

  // 닫기 버튼 클릭 시
  const handleClose = () => {
    navigate('/');
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
                      {applicant.name} ({applicant.gender})
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
              onClick={handleClose}
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

              {/* 점수 및 분석 카드 */}
              <div className="analysis-card">
                <div className="score-section">
                  <div className="score-header">
                    <Award className="score-icon" />
                    <h3 className="score-title">종합 평가 점수</h3>
                  </div>
                  <div className="score-number">{selectedApplicant.score}점</div>
                  <div className="score-bar">
                    <div 
                      className="score-bar-fill" 
                      style={{width: `${selectedApplicant.score}%`}}
                    />
                  </div>
                </div>

                <div className="overall-section">
                  <div className="overall-header">
                    <Target className="overall-icon" />
                    <h3 className="overall-title">종합 총평</h3>
                  </div>
                  <p className="overall-text">
                    {selectedApplicant.name} 지원자는 {selectedApplicant.category} 직무에 매우 적합한 후보자로 평가됩니다. 
                    탄탄한 기술적 기반 위에 실무 경험이 더해져 즉시 전력으로 활약할 수 있는 역량을 갖추고 있습니다. 
                    특히 {selectedApplicant.skills.required.slice(0, 2).join(', ')} 등의 기술에 대한 깊이 있는 이해와 
                    {selectedApplicant.skills.preferred[0]}을 통한 협업 경험은 팀 내에서 시너지를 창출할 수 있는 
                    강력한 자산이 될 것입니다.
                  </p>
                  <p className="overall-text">
                    또한, 지속적인 자기계발 의지와 새로운 기술에 대한 열린 자세는 빠르게 변화하는 
                    기술 환경에서 조직과 함께 성장할 수 있는 잠재력을 보여줍니다. 
                    {selectedApplicant.department} 전공 배경과 실무 경험이 조화를 이루어 
                    이론과 실무를 모두 아우르는 균형잡힌 개발자로 성장할 것으로 기대됩니다.
                  </p>
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
                      {selectedApplicant.skills.required.map((skill, idx) => (
                        <span key={idx} className="keyword-tag keyword-required">{skill}</span>
                      ))}
                    </div>
                  </div>

                  <div className="keywords-group">
                    <h4 className="keywords-group-title">우대 역량</h4>
                    <div className="keywords">
                      {selectedApplicant.skills.preferred.map((skill, idx) => (
                        <span key={idx} className="keyword-tag keyword-preferred">{skill}</span>
                      ))}
                    </div>
                  </div>

                  <div className="keywords-group">
                    <h4 className="keywords-group-title">사용 도구</h4>
                    <div className="keywords">
                      {selectedApplicant.skills.tools.map((skill, idx) => (
                        <span key={idx} className="keyword-tag keyword-tools">{skill}</span>
                      ))}
                    </div>
                  </div>
                </div>

                {/* 지원자 기본 정보 추가 */}
                <div className="applicant-info-summary">
                  <h4 className="info-summary-title">지원자 정보</h4>
                  <div className="info-summary-grid">
                    <div className="info-summary-item">
                      <span className="info-summary-label">이름</span>
                      <span className="info-summary-value">{selectedApplicant.name}</span>
                    </div>
                    <div className="info-summary-item">
                      <span className="info-summary-label">직무</span>
                      <span className="info-summary-value category-highlight">{selectedApplicant.category}</span>
                    </div>
                    <div className="info-summary-item">
                      <span className="info-summary-label">전공</span>
                      <span className="info-summary-value">{selectedApplicant.department}</span>
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