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
  onFilterCategory,
  categoryMap,
  onAnalyzeResume,
  analysisResult
}) {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      onFileChange(e.target.files[0]);
    }
  };

  // 카테고리 선택 핸들러
  const handleCategoryClick = (category) => {
    onSelectCategory(category);
    
    // 영문 카테고리 값 확인 (디버깅용)
    if (categoryMap) {
      console.log('Category button clicked:', category);
      console.log('English value:', categoryMap[category]);
    }
  };

  // 지원자 클릭 시 상세 페이지로 이동
  const handleApplicantClick = (applicant) => {
    navigate(`/applicant/${applicant.id}`);
  };

  // 이력서 분석 시작 버튼 클릭 핸들러 (E5 모델 호출)
  const handleAnalyzeClick = async () => {
    // 파일이 업로드되지 않은 경우 경고
    if (!file) {
      alert('먼저 이력서 파일을 업로드해주세요.');
      return;
    }

    // E5 모델에 job_role과 file 전송
    const jobRole = categoryMap[selectedCategory]; // 'frontend', 'backend', 'uiux'
    
    console.log('Starting resume analysis...');
    console.log('Job Role:', jobRole);
    console.log('File:', file.name);

    const result = await onAnalyzeResume(file, jobRole);
    
    if (result.success) {
      console.log('Analysis completed successfully!');
      console.log('Score:', result.data.score);
      console.log('Keywords:', result.data.keywords);
      
      // 분석 완료 후 결과 페이지로 이동하거나 모달 표시 가능
      alert(`분석 완료!\n점수: ${result.data.score}\n키워드: ${result.data.keywords}`);
    } else {
      console.error('Analysis failed:', result.error);
      alert('이력서 분석에 실패했습니다. 다시 시도해주세요.');
    }
  };

  // 점수순으로 정렬 (높은 순)
  const sortedApplicants = [...applicants].sort((a, b) => b.score - a.score);

  // 카테고리 필터링
  const filteredApplicants = filterCategory === '전체'
    ? sortedApplicants
    : sortedApplicants.filter(applicant => applicant.category === filterCategory);

  return (
    <div className="app-container">
      {/* Header */}
      <header className="header">
        <div className="header-content">
          <FileText className="header-icon" />
          <h1 className="header-title">ResuMatch</h1>
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
            
            <div className="applicant-list">
              {filteredApplicants.map((applicant) => (
                <div
                  key={applicant.id}
                  className="applicant-card"
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

        {/* Main Content */}
        <main className="main-content">
          {/* Category Selection */}
          <section className="section-category">
            <h2 className="section-title category-title">
              직무 카테고리를 선택하세요
            </h2>
            
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

          {/* File Upload */}
          <section className="section-upload">
            <h2 className="section-title upload-title">
              이력서를 업로드하세요
            </h2>
            
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
                    <p className="upload-title">
                      이력서를 드래그하세요.
                    </p>
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
                {file && (
                  <p className="file-selected">
                    선택된 파일: {file.name}
                  </p>
                )}
              </div>

              <button 
                className="analyze-button" 
                disabled={!file}
                onClick={handleAnalyzeClick}
              >
                이력서 분석 시작하기
              </button>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}