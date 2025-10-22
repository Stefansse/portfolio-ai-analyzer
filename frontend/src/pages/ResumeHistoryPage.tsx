import React, { useEffect, useState } from "react";
import { resumeApi } from "../api/axios"; // your configured axios instance
import { useAuth } from "../context/AuthContext";
import { FaFilePdf, FaFileWord, FaDownload } from "react-icons/fa";
import { motion } from "framer-motion";
import type { ResumeResponseDTO } from "../types/types";

export default function ResumeHistoryPage() {
  const { token } = useAuth();
  const [resumes, setResumes] = useState<ResumeResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Helper: format file size
  const formatFileSize = (bytes: number) => {
    if (!bytes) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  useEffect(() => {
    if (!token) return;

    const fetchResumes = async () => {
      try {
        setLoading(true);
        const res = await resumeApi.get<ResumeResponseDTO[]>("/api/resumes/user", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setResumes(res.data);
      } catch (err: any) {
        console.error(err);
        setError(err.response?.data?.message || "Failed to load resumes.");
      } finally {
        setLoading(false);
      }
    };

    fetchResumes();
  }, [token]);

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

  const getFileIcon = (filename: string) => {
    if (filename.endsWith(".pdf")) return <FaFilePdf className="text-red-400 text-2xl" />;
    if (filename.endsWith(".docx") || filename.endsWith(".doc"))
      return <FaFileWord className="text-blue-400 text-2xl" />;
    return <FaFilePdf className="text-white text-2xl" />;
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-900 via-purple-900 to-blue-900 p-6">
      <div className="max-w-6xl mx-auto">
        <h1 className="text-4xl font-extrabold mb-8 text-white text-center">
          Your Resume History
        </h1>

        {loading && <p className="text-center text-cyan-300 text-lg">Loading...</p>}
        {error && <p className="text-center text-red-400 text-lg">{error}</p>}

        {!loading && resumes.length === 0 && !error && (
          <p className="text-center text-white/70 text-lg">
            No resumes uploaded yet.
          </p>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {resumes.map((resume) => {
            const match = resume.matchEvaluation || {};
            const strengths: string[] = match.strengths || [];
            const weaknesses: string[] = match.weaknesses || [];
            const matchScore: number = match.match_score || 0;

            return (
              <motion.div
                key={resume.id}
                whileHover={{ scale: 1.03 }}
                className="bg-white/10 backdrop-blur-md rounded-3xl p-6 shadow-xl border border-white/20 flex flex-col justify-between gap-4 transition-all duration-300"
              >
                <div className="flex items-center gap-4">
                  {getFileIcon(resume.filename)}
                  <div className="flex-1">
                    <h2 className="text-xl font-bold text-white">{resume.filename}</h2>
                    <p className="text-white/70 text-sm">
                      Uploaded: {new Date(resume.uploadedAt).toLocaleString()}
                    </p>
                    <p className="text-white/70 text-sm">Size: {formatFileSize(resume.size || 0)}</p>
                  </div>
                </div>

                {/* Circular Match Score */}
                <div className="w-24 h-24 relative mx-auto my-4">
                  <svg className="rotate-[-90deg] w-24 h-24">
                    <circle
                      cx="48"
                      cy="48"
                      r="44"
                      className="stroke-white/20 fill-none stroke-4"
                    />
                    <circle
                      cx="48"
                      cy="48"
                      r="44"
                      className="stroke-cyan-400 fill-none stroke-4"
                      strokeDasharray={2 * Math.PI * 44}
                      strokeDashoffset={
                        2 * Math.PI * 44 * (1 - matchScore / 100)
                      }
                      style={{ transition: "stroke-dashoffset 1s ease-out" }}
                    />
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center text-white font-bold text-lg">
                    {matchScore}%
                  </div>
                </div>

                {/* Strengths & Weaknesses */}
                <div className="flex flex-wrap gap-2 mt-2">
                  {strengths.map((s, i) => (
                    <motion.span
                      key={i}
                      whileHover={{ scale: 1.1 }}
                      className="bg-green-400/30 text-green-100 px-3 py-1 rounded-full text-sm border border-green-300/20"
                    >
                      {s}
                    </motion.span>
                  ))}
                  {weaknesses.map((w, i) => (
                    <motion.span
                      key={i}
                      whileHover={{ scale: 1.1 }}
                      className="bg-red-400/30 text-red-100 px-3 py-1 rounded-full text-sm border border-red-300/20"
                    >
                      {w}
                    </motion.span>
                  ))}
                </div>

                <button
                  onClick={() => handleDownload(resume.url, resume.filename)}
                  className="mt-4 w-full flex items-center justify-center gap-2 bg-cyan-500/80 hover:bg-cyan-400/80 text-white px-4 py-2 rounded-2xl font-semibold shadow-lg transition-transform hover:scale-105"
                >
                  <FaDownload /> Download
                </button>
              </motion.div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
