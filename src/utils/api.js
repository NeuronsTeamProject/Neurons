// utils/api.js

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

/**
 * 지원자 목록 조회
 * - GET /api/applicants
 */
export const fetchApplicants = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/applicants`, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const data = await response.json();
    return { success: true, data };
  } catch (error) {
    console.error("Error fetching applicants:", error);
    return { success: false, error: error.message };
  }
};

/**
 * (선택) 단건 지원자 조회
 * - GET /api/applicants/{id}
 */
export const fetchApplicantById = async (id) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/applicants/${id}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const data = await response.json();
    return { success: true, data };
  } catch (error) {
    console.error("Error fetching applicant:", error);
    return { success: false, error: error.message };
  }
};

/**
 * 이력서 분석 요청
 * - POST /api/analyze
 * - multipart/form-data
 *   file: PDF
 *   job_role: "frontend" / "backend" / "uiux" ...
 */
export const analyzeResume = async (file, jobRole) => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("job_role", jobRole);

    const response = await fetch(`${API_BASE_URL}/api/analyze`, {
      method: "POST",
      body: formData,
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const data = await response.json();
    return { success: true, data };
  } catch (error) {
    console.error("Error analyzing resume:", error);
    return { success: false, error: error.message };
  }
};