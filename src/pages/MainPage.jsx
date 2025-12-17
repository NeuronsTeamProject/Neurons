// pages/MainPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Code, Briefcase, Upload, ChevronLeft, ChevronRight } from 'lucide-react';
import '../styles/MainPage.css';

export default function MainPage({
  applicants,
  selectedCategory,
  onSelectCategory,
  file,
  onFileChange,
  filterCategory,
  onAnalyzeResume
}) {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      onFileChange(e.target.files[0]);
    }
  };

  const getDisplayName = (applicant) => {
    if (applicant.name) return applicant.name;
    if (applicant.pdfName) return applicant.pdfName.replace(/\.[^/.]+$/, '');
    return `지원자 ${applicant.id}`;
  };

  // ✅ 버튼(표시용) -> DB 저장용 role(job_role)
  const toDbRole = (category) => {
    if (category === '기획자') return 'UI/UX';
    if (category === '프론트엔드') return '프론트엔드';
    if (category === '백엔드') return '백엔드';
    return category || '';
  };

  const handleCategoryClick = (category) => {
    onSelectCategory(category);
    console.log('[UI] selectedCategory =', category);
    console.log('[UI] jobRole(to DB) =', toDbRole(category));
  };

  const handleApplicantClick = (applicant) => {
    navigate(`/applicant/${applicant.id}`);
  };

  // ✅ 분석 시작: 성공 시 바로 Detail 이동 (alert 없음)
  const handleAnalyzeClick = async () => {
    if (!selectedCategory) {
      alert('직무 카테고리를 먼저 선택해주세요.');
      return;
    }
    if (!file) {
      alert('먼저 이력서 파일을 업로드해주세요.');
      return;
    }

    const jobRole = toDbRole(selectedCategory);

    const result = await onAnalyzeResume(file, jobRole);

    if (result?.success) {
      const newId = result.data?.id;

      if (newId) {
        navigate(`/applicant/${newId}`);
        return;
      }

      console.warn('[Analyze] 성공했지만 result.data.id가 없음. 응답 DTO에 id 포함 필요');
    } else {
      console.error('[Analyze] failed:', result?.error);
      alert('이력서 분석에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const sortedApplicants = [...applicants].sort((a, b) => (b.score ?? 0) - (a.score ?? 0));

  const filteredApplicants = filterCategory === '전체'
    ? sortedApplicants
    : sortedApplicants.filter((applicant) => applicant.role === filterCategory);

  return (
    <div className="app-zoom-wrapper">
      <div className="app-zoom-content">
        <div className="app-container">
          <header className="header">
            <div className="header-content">
              <FileText className="header-icon" />
              <h1 className="header-title">ResuMatch</h1>
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

                <div className="applicant-list">
                  {filteredApplicants.map((applicant) => (
                    <div
                      key={applicant.id}
                      className="applicant-card"
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

            <main className="main-content">
              <section className="section-category">
                <h2 className="section-title category-title">직무 카테고리를 선택하세요</h2>

                <div className="category-grid">
                  <button
                    className={`category-button ${selectedCategory === '백엔드' ? 'selected' : ''}`}
                    onClick={() => handleCategoryClick('백엔드')}
                  >
                    <div className="category-content">
                      <Code className="category-icon" />
                      <span className="category-label">백엔드</span>
                    </div>
                  </button>

                  <button
                    className={`category-button ${selectedCategory === '프론트엔드' ? 'selected' : ''}`}
                    onClick={() => handleCategoryClick('프론트엔드')}
                  >
                    <div className="category-content">
                      <Code className="category-icon" />
                      <span className="category-label">프론트엔드</span>
                    </div>
                  </button>

                  <button
                    className={`category-button ${selectedCategory === '기획자' ? 'selected' : ''}`}
                    onClick={() => handleCategoryClick('기획자')}
                  >
                    <div className="category-content">
                      <Briefcase className="category-icon" />
                      <span className="category-label">기획자</span>
                    </div>
                  </button>
                </div>
              </section>

              <section className="section-upload">
                <h2 className="section-title upload-title">이력서를 업로드하세요</h2>

                <div className="upload-container">
                  <div className="upload-area">
                    <input
                      type="file"
                      id="resume-upload"
                      className="upload-input"
                      accept=".pdf"
                      onChange={handleFileChange}
                    />
                    <label htmlFor="resume-upload" className="upload-label">
                      <Upload className="upload-icon" />
                      <div className="upload-text">
                        <p className="upload-title">이력서를 드래그하세요.</p>
                        <p className="upload-subtitle">PDF 파일만 지원됩니다</p>
                      </div>
                      <button
                        className="file-select-button"
                        type="button"
                        onClick={(e) => {
                          e.preventDefault();
                          document.getElementById('resume-upload').click();
                        }}
                      >
                        파일 선택
                      </button>
                    </label>

                    {file && <p className="file-selected">선택된 파일: {file.name}</p>}
                  </div>

                  <button
                    className="analyze-button"
                    disabled={!file || !selectedCategory}
                    onClick={handleAnalyzeClick}
                  >
                    이력서 분석 시작하기
                  </button>
                </div>
              </section>
            </main>
          </div>
        </div>
      </div>
    </div>
  );
}
