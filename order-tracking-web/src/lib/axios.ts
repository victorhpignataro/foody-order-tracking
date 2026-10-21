import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const timeout = 30000;

export const apiAuth = axios.create({ baseURL, timeout });

export const api = axios.create({
  baseURL,
  withCredentials: true,
  timeout,
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (
      error.response?.status === 401 &&
      localStorage.getItem('userIsLoggedUI')
    ) {
      localStorage.removeItem('userIsLoggedUI');
      window.location.assign('/login');
    }
    return Promise.reject(error);
  },
);
