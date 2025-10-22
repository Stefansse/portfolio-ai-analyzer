import React, { useState } from "react";
import { resumeApi } from "../api/axios";
import type { ResumeResponseDTO } from "../types/types";
import { useAuth } from "../context/AuthContext";

interface ResumeUploadPageProps {
  onUploadSuccess?: () => void; // callback to notify Dashboard
}

export default function ResumeUploadPage({ onUploadSuccess }: ResumeUploadPageProps) {
  const [file, setFile] = useState<File | null>(null);
  const [jobDescription, setJobDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState<ResumeResponseDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const { token } = useAuth();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setFile(e.target.files?.[0] || null);

  const handleUpload = async () => {
    if (!token) return (window.location.href = "/auth");
    if (!file) return setError("Please select a file first.");

    const formData = new FormData();
    formData.append("file", file);
    formData.append("jobDescription", jobDescription);

    try {
      setLoading(true);
      setError(null);
      setSuccess(false);

      const res = await resumeApi.post<ResumeResponseDTO>(
        "/api/resumes/upload",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setResponse(res.data);
      setSuccess(true);

      // ✅ Trigger dashboard refresh
      if (onUploadSuccess) onUploadSuccess();

    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || "Upload failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (url: string, filename: string) => {
    try {
      const res = await fetch(url);
      const blob = await res.blob();
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error("Download failed:", err);
    }
  };

  return (
    <div className="max-w-3xl mx-auto mt-12 p-10 rounded-3xl shadow-xl text-white bg-white/10 backdrop-blur-lg transition-all duration-500">

      {/* ✨ Intro Section */}
      <div className="text-center mb-10">
        <h1 className="text-4xl font-bold mb-3 text-cyan-200">
          Welcome to <span className="text-white">ResumeAI Analyzer</span> 🚀
        </h1>
        <p className="text-white/80 text-lg leading-relaxed max-w-2xl mx-auto">
          Upload your resume and let our AI evaluate how well it matches your
          target job. You’ll receive detailed insights, skill analysis, and a
          match score tailored to your career goals.
        </p>
      </div>

      {/* 🧠 Upload Form */}
      <h2 className="text-2xl font-semibold mb-6 text-center">Upload Your Resume</h2>

      <div className="flex flex-col gap-5">
        <div className="flex flex-col items-center justify-center space-y-3">
          <label
            htmlFor="fileInput"
            className="cursor-pointer bg-cyan-600/40 hover:bg-cyan-500/60 transition-all text-white font-semibold py-3 px-6 rounded-2xl shadow-lg backdrop-blur-md border border-cyan-400/20 hover:scale-105"
          >
            Choose Resume File
          </label>
          <input
            id="fileInput"
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={handleFileChange}
            disabled={loading}
            className="hidden"
          />
          {file && (
            <p className="text-sm text-cyan-100/90 font-medium mt-1">
              Selected: <span className="text-white">{file.name}</span>
            </p>
          )}
        </div>

        <textarea
          placeholder="Paste job description (optional)"
          value={jobDescription}
          onChange={(e) => setJobDescription(e.target.value)}
          disabled={loading}
          className="border border-white/30 p-3 rounded-xl bg-white/10 text-white placeholder-white h-36 resize-none transition focus:border-cyan-300 focus:ring-1 focus:ring-cyan-300"
        />

        <button
          onClick={handleUpload}
          disabled={loading}
          className="relative bg-gradient-to-r from-cyan-500 via-teal-400 to-green-400 py-3 rounded-2xl font-semibold text-white text-lg hover:scale-105 transition-transform disabled:opacity-50 flex justify-center items-center shadow-lg"
        >
          {loading ? (
            <div className="flex items-center gap-2">
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              <span>Processing your resume...</span>
            </div>
          ) : (
            "Upload Resume"
          )}
        </button>

        {error && <p className="text-red-400 font-medium text-sm text-center">{error}</p>}
        {success && <p className="text-green-300 font-semibold text-center animate-pulse">Upload Complete ✅</p>}
      </div>

      {/* 💡 AI Response */}
      {response && (
        <div className="mt-10 animate-fade-up">
          {/* Resume Info */}
          <div className="bg-white/10 backdrop-blur-md p-6 rounded-2xl shadow-lg border border-white/20 mb-6">
            <h3 className="text-2xl font-semibold mb-3">Your Uploaded Resume</h3>
            <p><strong>Filename:</strong> {response.filename}</p>
            <p><strong>Uploaded:</strong> {new Date(response.uploadedAt).toLocaleString()}</p>

            <button
              onClick={() => handleDownload(response.url, response.filename)}
              className="inline-block mt-4 bg-gradient-to-r from-cyan-400 via-teal-400 to-green-400 hover:from-teal-400 hover:to-cyan-400 text-white font-semibold py-2 px-5 rounded-xl shadow-md transition-transform hover:scale-105"
            >
              Download
            </button>
          </div>

          {/* 🧠 AI Evaluation */}
          {response.matchEvaluation ? (
            <div className="relative overflow-hidden rounded-2xl p-6 bg-gradient-to-br from-cyan-800/40 to-teal-700/40 border border-cyan-300/20 shadow-2xl backdrop-blur-md">
              <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(0,255,255,0.15),transparent)]"></div>
              <h3 className="text-2xl font-bold mb-5 text-cyan-200 drop-shadow">
                🤖 AI Evaluation
              </h3>

              {/* Match Score */}
              <div className="mb-5">
                <p className="font-semibold mb-1">Match Score:</p>
                <div className="w-full bg-white/10 rounded-full h-4 overflow-hidden">
                  <div
                    className="h-4 bg-gradient-to-r from-green-400 via-cyan-400 to-blue-400 transition-all duration-700"
                    style={{ width: `${response.matchEvaluation.match_score}%` }}
                  ></div>
                </div>
                <p className="text-sm text-cyan-100 mt-1">
                  {response.matchEvaluation.match_score} match with job description
                </p>
              </div>

              {/* Summary */}
              <div className="bg-white/10 p-4 rounded-xl border border-white/10 italic text-cyan-100 leading-relaxed shadow-inner">
                “{response.matchEvaluation.summary}”
              </div>

              {/* Strengths */}
              {response.matchEvaluation.strengths?.length > 0 && (
                <div className="mt-6">
                  <p className="font-semibold text-green-300 mb-2">Key Strengths:</p>
                  <div className="flex flex-wrap gap-2">
                    {response.matchEvaluation.strengths.map((s, i) => (
                      <span
                        key={i}
                        className="bg-green-400/20 text-green-100 px-3 py-1 rounded-full text-sm border border-green-300/20"
                      >
                        {s}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Weaknesses */}
              {response.matchEvaluation.weaknesses?.length > 0 && (
                <div className="mt-6">
                  <p className="font-semibold text-red-300 mb-2">Weaknesses:</p>
                  <div className="flex flex-wrap gap-2">
                    {response.matchEvaluation.weaknesses.map((w, i) => (
                      <span
                        key={i}
                        className="bg-red-400/20 text-red-100 px-3 py-1 rounded-full text-sm border border-red-300/20"
                      >
                        {w}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="bg-yellow-400/10 border border-yellow-400/30 rounded-2xl p-6 text-yellow-200 text-center shadow-lg">
              ⚠️ No job description was provided.  
              For a detailed match analysis, please include a job description next time.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
