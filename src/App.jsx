// App.jsx
import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import ApplicantDetail from './pages/ApplicantDetail';
import './App.css';

export default function App() {
  const [selectedCategory, setSelectedCategory] = useState('프론트엔드');
  const [file, setFile] = useState(null);
  const [filterCategory, setFilterCategory] = useState('전체');
  const [applicants, setApplicants] = useState([]); // 빈 배열로 시작
  const [analysisResult, setAnalysisResult] = useState(null); // 분석 결과 저장

  // 직무 카테고리 한글-영문 매핑 (E5 모델에 맞게)
  const categoryMap = {
    '프론트엔드': 'frontend',
    '백엔드': 'backend',
    '기획자': 'uiux'
  };

  // 백엔드에서 지원자 데이터 가져오기
  const fetchApplicants = async () => {
    try {
      const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';
      
      const response = await fetch(`${API_URL}/api/applicants`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setApplicants(data.applicants || data || []);
      console.log('Applicants loaded:', data);
    } catch (error) {
      console.error('Error fetching applicants:', error);
      setApplicants([]);
    }
  };

  // E5 모델에 이력서 분석 요청 (job_role + file)
  const analyzeResumeWithE5 = async (file, jobRole) => {
    try {
      const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';
      
      const formData = new FormData();
      formData.append('file', file); // 파라미터 이름: file
      formData.append('job_role', jobRole); // 파라미터 이름: job_role
      
      console.log('Sending to E5 model:', { file: file.name, job_role: jobRole });

      const response = await fetch(`${API_URL}/api/analyze`, {
        method: 'POST',
        body: formData
        // FormData 사용 시 Content-Type 헤더는 자동 설정됨
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      // 응답: { score: number, keywords: string }
      console.log('E5 Analysis Result:', data);
      
      setAnalysisResult({
        score: data.score,
        keywords: data.keywords // "리더십, 협업, DB, ..., java"
      });

      return {
        success: true,
        data: data
      };
    } catch (error) {
      console.error('Error analyzing resume with E5:', error);
      return {
        success: false,
        error: error.message
      };
    }
  };

  // 백엔드로 카테고리 전송하는 함수 (선택적으로 사용)
  const sendCategoryToBackend = async (englishCategory) => {
    try {
      // 백엔드 API URL (환경에 따라 변경)
      const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';
      
      const response = await fetch(`${API_URL}/api/category`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          category: englishCategory,
          timestamp: new Date().toISOString()
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return {
        success: true,
        data: data
      };
    } catch (error) {
      console.error('Error sending category to backend:', error);
      return {
        success: false,
        error: error.message
      };
    }
  };

  // 카테고리 선택 핸들러
  const handleCategorySelect = async (koreanCategory) => {
    setSelectedCategory(koreanCategory);
    const englishCategory = categoryMap[koreanCategory];
    
    // 콘솔에 출력 (디버깅용)
    console.log('Selected category (Korean):', koreanCategory);
    console.log('Selected category (English):', englishCategory);
    
    // 백엔드로 전송
    const result = await sendCategoryToBackend(englishCategory);
    
    if (result.success) {
      console.log('Category sent successfully:', result.data);
    } else {
      console.error('Failed to send category:', result.error);
    }
  };

  // 컴포넌트 마운트 시 지원자 데이터 로드
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
            <div style={{ 
              display: 'flex', 
              flexDirection: 'column',
              alignItems: 'center', 
              justifyContent: 'center', 
              height: '100vh',
              gap: '1rem'
            }}>
              <h2>페이지를 찾을 수 없습니다</h2>
              <a href="/" style={{ color: '#8AA399' }}>메인으로 돌아가기</a>
            </div>
          } 
        />
      </Routes>
    </BrowserRouter>
  );
}