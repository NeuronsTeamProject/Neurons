// App.jsx
import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import ApplicantDetail from './pages/ApplicantDetail';
import './App.css';

const applicants = [
  {
    id: 1,
    name: '홍길동',
    gender: 'M',
    score: 85,
    category: '프론트엔드',
    phone: '010-1234-5678',
    location: '서울 강남구',
    department: '컴퓨터공학과',
    introduction: '안녕하세요. 저는 React 와 Node.js 를 활용한 웹서비스 개발에 관심이 많은 개발자입니다. 다양한 프로젝트들 경험하며 기업의 다양한 프로젝트들 통해 실무 경험을 많이 쌓았습니다. 특히 JavaScript 와 TypeScript 을 이용하여 웹 개발에 능숙한 가지고 있으며, 사용자 경험 중심의 개발자가 되겠습니다.',
    skills: {
      required: ['React', 'Node.js', 'JavaScript', 'TypeScript'],
      preferred: ['Git', '협업', 'Agile'],
      tools: ['Docker', 'AWS']
    }
  },
  {
    id: 2,
    name: '김영희',
    gender: 'F',
    score: 72,
    category: '백엔드',
    phone: '010-2345-6789',
    location: '부산 해운대구',
    department: '정보통신학과',
    introduction: '웹 개발에 관심이 많으며, 최신 기술 트렌드를 따라가는 것을 좋아합니다. Python과 Django를 기본으로 시작했으며, 현재는 풀스택 개발자로 성장하고 싶습니다.',
    skills: {
      required: ['Python', 'Django', 'HTML/CSS'],
      preferred: ['React', '데이터베이스', 'REST API'],
      tools: ['GitHub', 'VS Code']
    }
  },
  {
    id: 3,
    name: '박민수',
    gender: 'M',
    score: 68,
    category: '백엔드',
    phone: '010-3456-7890',
    location: '대구 중구',
    department: '소프트웨어학과',
    introduction: '자바 기반의 백엔드 개발에 전문성을 가지고 있습니다. 마이크로서비스 아키텍처에 관심이 있으며, 클라우드 환경에서의 배포 경험이 있습니다.',
    skills: {
      required: ['Java', 'Spring', 'SQL'],
      preferred: ['Kubernetes', '마이크로서비스', 'CI/CD'],
      tools: ['Jenkins', 'Grafana']
    }
  },
  {
    id: 4,
    name: '이수진',
    gender: 'F',
    score: 91,
    category: '프론트엔드',
    phone: '010-4567-8901',
    location: '인천 남동구',
    department: '컴퓨터정보학과',
    introduction: '프론트엔드 개발자로서 사용자 경험을 최우선으로 생각합니다. Vue.js와 React를 모두 경험했으며, 접근성과 반응형 디자인에 관심이 많습니다.',
    skills: {
      required: ['React', 'Vue.js', 'CSS', 'JavaScript'],
      preferred: ['UI/UX', '성능최적화', 'Testing'],
      tools: ['Figma', 'Webpack']
    }
  },
  {
    id: 5,
    name: '최준호',
    gender: 'M',
    score: 76,
    category: '기획자',
    phone: '010-5678-9012',
    location: '광주 동구',
    department: '정보보안학과',
    introduction: '데이터 분석과 머신러닝에 관심이 있는 개발자입니다. Python과 R을 활용하여 데이터 처리 및 시각화 작업을 수행합니다.',
    skills: {
      required: ['Python', 'pandas', 'NumPy'],
      preferred: ['TensorFlow', '데이터시각화', 'SQL'],
      tools: ['Jupyter', 'Tableau']
    }
  },
  {
    id: 6,
    name: '정민지',
    gender: 'F',
    score: 88,
    category: '프론트엔드',
    phone: '010-6789-0123',
    location: '서울 서초구',
    department: '디자인학과',
    introduction: 'UI/UX 디자인 배경을 가진 프론트엔드 개발자입니다. 사용자 중심의 인터페이스 구현에 강점이 있습니다.',
    skills: {
      required: ['React', 'TypeScript', 'Tailwind'],
      preferred: ['Figma', '디자인시스템', 'Storybook'],
      tools: ['Git', 'Vercel']
    }
  },
  {
    id: 7,
    name: '강태현',
    gender: 'M',
    score: 79,
    category: '백엔드',
    phone: '010-7890-1234',
    location: '대전 유성구',
    department: '컴퓨터공학과',
    introduction: 'Node.js 기반 백엔드 개발자로, RESTful API 설계 및 구현에 능숙합니다.',
    skills: {
      required: ['Node.js', 'Express', 'MongoDB'],
      preferred: ['GraphQL', 'Redis', 'Docker'],
      tools: ['Postman', 'AWS']
    }
  }
];

export default function App() {
  const [selectedCategory, setSelectedCategory] = useState('프론트엔드');
  const [file, setFile] = useState(null);
  const [filterCategory, setFilterCategory] = useState('전체');

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
              onSelectCategory={setSelectedCategory}
              file={file}
              onFileChange={setFile}
              filterCategory={filterCategory}
              onFilterCategory={setFilterCategory}
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