// utils/api.js

// 백엔드 API 기본 URL (환경에 따라 변경)
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';

/**
 * 직무 카테고리를 백엔드로 전송
 * @param {string} category - 영문 카테고리 값 ('frontend', 'backend', 'uiux')
 * @returns {Promise<Object>} API 응답
 */
export const sendCategoryToBackend = async (category) => {
    try {
        const response = await fetch(`${API_BASE_URL}/api/category`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            category: category,
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

/**
 * 이력서 파일과 카테고리를 함께 백엔드로 전송
 * @param {File} file - PDF 파일
 * @param {string} category - 영문 카테고리 값
 * @returns {Promise<Object>} API 응답
 */
export const uploadResumeWithCategory = async (file, category) => {
    try {
        const formData = new FormData();
        formData.append('resume', file);
        formData.append('category', category);

        const response = await fetch(`${API_BASE_URL}/api/upload-resume`, {
        method: 'POST',
        body: formData
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
        console.error('Error uploading resume:', error);
        return {
        success: false,
        error: error.message
        };
    }
};

/**
 * 지원자 목록 조회
 * @returns {Promise<Object>} 지원자 목록
 */
export const fetchApplicants = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/api/applicants`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
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
        console.error('Error fetching applicants:', error);
        return {
        success: false,
        error: error.message
        };
    }
};

/**
 * 카테고리별 지원자 목록 조회
 * @param {string} category - 영문 카테고리 값 (선택사항)
 * @returns {Promise<Object>} 지원자 목록
 */
export const fetchApplicantsByCategory = async (category = null) => {
    try {
        const url = category 
        ? `${API_BASE_URL}/api/applicants?category=${category}`
        : `${API_BASE_URL}/api/applicants`;

        const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
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
        console.error('Error fetching applicants:', error);
        return {
        success: false,
        error: error.message
        };
    }
};

/**
 * 이력서 분석 요청
 * @param {File} file - PDF 파일
 * @param {string} category - 영문 카테고리 값
 * @returns {Promise<Object>} 분석 결과
 */
export const analyzeResume = async (file, category) => {
    try {
        const formData = new FormData();
        formData.append('resume', file);
        formData.append('category', category);

        const response = await fetch(`${API_BASE_URL}/api/analyze-resume`, {
        method: 'POST',
        body: formData
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
        console.error('Error analyzing resume:', error);
        return {
        success: false,
        error: error.message
        };
    }
};