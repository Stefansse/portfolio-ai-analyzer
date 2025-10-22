import axios from "axios";
import type { AxiosInstance } from "axios";

// ----------------------------
// User Service
// ----------------------------
export const userApi: AxiosInstance = axios.create({
  baseURL: "http://localhost:8081",
  headers: { "Content-Type": "application/json" },
});

// ----------------------------
// Resume Service
// ----------------------------
export const resumeApi: AxiosInstance = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

// ----------------------------
// Resume Analyzer Service
// ----------------------------
export const analyzerApi: AxiosInstance = axios.create({
  baseURL: "http://localhost:8082",
  headers: { "Content-Type": "application/json" },
});

// ----------------------------
// JWT Interceptor
// ----------------------------
const attachToken = (apiInstance: AxiosInstance) => {
  apiInstance.interceptors.request.use((config) => {
    const token = localStorage.getItem("jwtToken");
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
};

// Apply interceptor to all services
[userApi, resumeApi, analyzerApi].forEach((api) => attachToken(api));
