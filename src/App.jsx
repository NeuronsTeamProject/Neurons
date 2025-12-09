// App.jsx
import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import ApplicantDetail from './pages/ApplicantDetail';
import './App.css';

// 공통 API URL
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export default function App() {
  const [selectedCategory, setSelectedCategory] = useState('프론트엔드');
  const [file, setFile] = useState(null);
  const [filterCategory, setFilterCategory] = useState('전체');
  const [applicants, setApplicants] = useState([]);
  const [analysisResult, setAnalysisResult] = useState(null);

  // 한글 ↔ 영문 직무 매핑 (E5용)
  const categoryMap = {
    '프론트엔드': 'frontend',
    '백엔드': 'backend',
    '기획자': 'uiux',
  };

  // 1) 백엔드에서 지원자 목록 가져오기
  const fetchApplicants = async () => {
    try {
      const response = await fetch(`${API_URL}/api/applicants`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      // 컨트롤러가 List<ApplicantResponseDTO>를 바로 넘기므로 그대로 사용
      setApplicants(Array.isArray(data) ? data : []);
      console.log('Applicants loaded:', data);
    } catch (error) {
      console.error('Error fetching applicants:', error);
      setApplicants([]);
    }
  };

  // 2) E5 모델에 이력서 분석 요청
  const analyzeResumeWithE5 = async (file, jobRole) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('job_role', jobRole);

      console.log('Sending to E5 model:', {
        file: file?.name,
        job_role: jobRole,
      });

      const response = await fetch(`${API_URL}/api/analyze`, {
        method: 'POST',
        body: formData, // Content-Type 자동 설정
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();

      // 응답 예: { id, pdfName, role, score, keywords, aiSummary }
      setAnalysisResult({
        score: data.score,
        keywords: data.keywords,
        aiSummary: data.aiSummary,
      });

      // 분석 이후 목록도 다시 불러오면, 새로 저장된 지원자가 리스트에 반영됨
      fetchApplicants();

      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error('Error analyzing resume with E5:', error);
      return {
        success: false,
        error: error.message,
      };
    }
  };

  // 3) 카테고리 선택 (이제 백엔드로 안 보냄)
  const handleCategorySelect = (koreanCategory) => {
    setSelectedCategory(koreanCategory);
    const englishCategory = categoryMap[koreanCategory];

    console.log('Selected category (Korean):', koreanCategory);
    console.log('Selected category (English):', englishCategory);
    // 더 이상 /api/category 로 보내지 않음
  };

  // 마운트 시 지원자 목록 한 번 로딩
  useEffect(() => {
    fetchApplicants();
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        {/* 메인 페이지 */}
        <Route
          path="/"
          element={
            <MainPage
              applicants={applicants}
              selectedCategory={selectedCategory}
              onSelectCategory={handleCategorySelect}
              file={file}
              onFileChange={setFile}
              filterCategory={filterCategory}
              onFilterCategory={setFilterCategory}
              categoryMap={categoryMap}
              onAnalyzeResume={analyzeResumeWithE5}
              analysisResult={analysisResult}
            />
          }
        />

        {/* 지원자 상세 페이지 */}
        <Route
          path="/applicant/:id"
          element={
            <ApplicantDetail
              applicants={applicants}
              file={file}
              filterCategory={filterCategory}
              onFilterCategory={setFilterCategory}
            />
          }
        />

        {/* 404 페이지 */}
        <Route
          path="*"
          element={
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100vh',
                gap: '1rem',
              }}
            >
              <h2>페이지를 찾을 수 없습니다</h2>
              <a href="/" style={{ color: '#8AA399' }}>
                메인으로 돌아가기
              </a>
            </div>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
