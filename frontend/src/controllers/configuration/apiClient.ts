import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 1000000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('bathforge_auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('API request error:', error);
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API response error:', error.response?.status, error.response?.data || error.message);
    if (error.response?.status === 401) {
      localStorage.removeItem('bathforge_auth_token');
      localStorage.removeItem('bathforge_user');
    }
    return Promise.reject(error);
  }
);

export { apiClient };
export default apiClient;
