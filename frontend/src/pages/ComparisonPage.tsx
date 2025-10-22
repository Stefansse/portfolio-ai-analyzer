import React, { useState } from "react";
import { resumeApi } from "../api/axios";
import { AiOutlineCheckCircle } from "react-icons/ai";
import { motion } from "framer-motion";

export default function ComparisonPage() {
  const [file1, setFile1] = useState<File | null>(null);
  const [file2, setFile2] = useState<File | null>(null);
  const [jobDescription, setJobDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  const handleCompare = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file1 || !file2) return alert("Please upload both resumes");

    const formData = new FormData();
    formData.append("file1", file1);
    formData.append("file2", file2);
    formData.append("jobDescription", jobDescription);

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await resumeApi.post("/api/resumes/compare-resumes", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setResult(response.data);
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.error || "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-cyan-800 via-cyan-700 to-teal-700 text-white flex justify-center items-start py-12 overflow-x-hidden relative">
      
      {/* Optional floating particles / abstract shapes */}
      <div className="absolute top-0 left-0 w-full h-full pointer-events-none">
        <div className="animate-pulse absolute top-20 left-10 w-24 h-24 bg-cyan-400/20 rounded-full blur-3xl"></div>
        <div className="animate-pulse absolute top-60 right-20 w-32 h-32 bg-teal-400/20 rounded-full blur-3xl"></div>
      </div>

      <div className="relative w-full max-w-6xl space-y-12 z-10">

        <motion.h1
          initial={{ opacity: 0, y: -40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="text-5xl md:text-6xl font-extrabold text-center text-white drop-shadow-lg"
        >
          Compare Resumes
        </motion.h1>

        {/* Upload Form */}
        <motion.form
          onSubmit={handleCompare}
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.2 }}
          className="bg-white/10 backdrop-blur-3xl border border-white/20 rounded-3xl p-8 shadow-2xl flex flex-col gap-6"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[{label: "Resume 1", file: file1, setter: setFile1}, {label: "Resume 2", file: file2, setter: setFile2}].map((item, idx) => (
              <div key={idx} className="flex flex-col gap-2">
                <label className="font-bold text-lg">{item.label}:</label>
                <input
                  type="file"
                  onChange={(e) => item.setter(e.target.files?.[0] || null)}
                  accept=".pdf,.docx"
                  className="file:mr-4 file:py-3 file:px-5 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-gradient-to-r file:from-cyan-500 file:to-teal-500 file:text-white hover:file:scale-105 transition-all duration-300"
                />
              </div>
            ))}
          </div>

          <div className="flex flex-col gap-2">
            <label className="font-bold text-lg">Job Description (optional):</label>
            <textarea
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              placeholder="Paste job description..."
              rows={4}
              className="w-full rounded-2xl p-4 bg-white/10 border border-white/30 placeholder-white/70 focus:outline-none focus:ring-2 focus:ring-cyan-400 shadow-inner transition-all duration-300"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 font-bold rounded-3xl bg-gradient-to-r from-cyan-500 to-teal-400 hover:from-cyan-600 hover:to-teal-500 shadow-xl hover:scale-105 transition-transform flex justify-center items-center gap-2"
          >
            {loading ? "Comparing..." : "Compare Resumes"}
          </button>
        </motion.form>

        {error && <div className="text-red-400 text-center font-bold">{error}</div>}

        {result && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.8 }}
            className="space-y-8"
          >
            {/* Recommendation */}
            <motion.div
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              transition={{ duration: 0.8, type: "spring", stiffness: 90 }}
              className="flex items-center gap-4 bg-green-500/20 border-l-4 border-green-400 p-5 rounded-2xl text-green-100 text-xl font-semibold shadow-xl animate-pulse"
            >
              <AiOutlineCheckCircle size={28} />
              Recommended Resume: {result.recommendation}
            </motion.div>

            {/* Resume Comparison */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {[1, 2].map((num) => (
                <motion.div
                  key={num}
                  initial={{ y: 20, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ duration: 0.8, delay: num * 0.1 }}
                  className="bg-white/10 backdrop-blur-xl p-6 rounded-3xl shadow-2xl hover:shadow-3xl hover:scale-105 transition-transform duration-300 border border-white/20"
                >
                  <h2 className="font-extrabold text-3xl text-cyan-300 mb-3">Resume {num}</h2>
                  <p className="text-lg"><span className="font-semibold">Match Score:</span> {num === 1 ? result.resume1.match_score : result.resume2.match_score}</p>
                  <p className="text-lg"><span className="font-semibold">Strengths:</span> {(num === 1 ? result.resume1.strengths : result.resume2.strengths).join(", ")}</p>
                  <p className="text-lg"><span className="font-semibold">Weaknesses:</span> {(num === 1 ? result.resume1.weaknesses : result.resume2.weaknesses).join(", ")}</p>
                </motion.div>
              ))}
            </div>

            {/* Skills Comparison */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {[
                { title: "Common Skills", skills: result.common_skills, bg: "bg-cyan-600/20" },
                { title: "Resume 1 Only", skills: result.differences.resume1_only, bg: "bg-indigo-600/20" },
                { title: "Resume 2 Only", skills: result.differences.resume2_only, bg: "bg-teal-500/20" }
              ].map((card, idx) => (
                <motion.div
                  key={idx}
                  initial={{ y: 20, opacity: 0 }}
                  animate={{ y: 0, opacity: 1 }}
                  transition={{ duration: 0.8, delay: idx * 0.2 }}
                  className={`${card.bg} p-6 rounded-2xl shadow-xl backdrop-blur-md hover:scale-105 transition-transform`}
                >
                  <h3 className="font-bold mb-2 text-lg">{card.title}</h3>
                  <ul className="list-disc list-inside space-y-1">
                    {card.skills.map((skill: string) => <li key={skill}>{skill}</li>)}
                  </ul>
                </motion.div>
              ))}
            </div>
          </motion.div>
        )}
      </div>
    </div>
  );
}
