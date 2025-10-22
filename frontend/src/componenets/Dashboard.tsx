import React, { useEffect, useState, useCallback } from "react";
import { Line, Bar, Pie } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { FaFileAlt, FaArrowUp, FaArrowDown, FaDownload } from "react-icons/fa";
import { useAuth } from "../context/AuthContext";
import type { AnalyticsDTO } from "../types/types";
import { analyzerApi } from "../api/axios";
import { motion } from "framer-motion";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

export default function Dashboard({ refreshFlag }: { refreshFlag?: boolean }) {
  const { token } = useAuth();
  const [analytics, setAnalytics] = useState<AnalyticsDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [darkMode, setDarkMode] = useState(true);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // ----- Fetch Analytics -----
  const fetchAnalytics = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    try {
      const storedUserId = localStorage.getItem("userId");
      if (!storedUserId) throw new Error("UserId not found in localStorage");

      const numericUserId = Number(storedUserId);
      if (isNaN(numericUserId)) throw new Error("Invalid numeric userId");

      const res = await analyzerApi.get<AnalyticsDTO[]>(
        `/analytics/user/${numericUserId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAnalytics(res.data);
    } catch (err) {
      console.error(err);
      setError("Failed to load analytics data.");
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics, refreshFlag]);

  // ----- Filtered Analytics -----
  const filteredAnalytics = analytics.filter((a) => {
    const date = new Date(a.uploadedAt);
    return (
      (!startDate || date >= new Date(startDate)) &&
      (!endDate || date <= new Date(endDate))
    );
  });

  // ----- Helper Functions -----
  const flattenSkills = (listOfLists: string[][]) =>
    listOfLists
      .flat()
      .join(",")
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);

  const countOccurrences = (arr: string[]) =>
    arr.reduce<Record<string, number>>((acc, skill) => {
      if (!skill) return acc;
      acc[skill] = (acc[skill] || 0) + 1;
      return acc;
    }, {});

  const strongSkillsAll = flattenSkills(
    filteredAnalytics.map((a) => a.strongSkills)
  );
  const weakSkillsAll = flattenSkills(
    filteredAnalytics.map((a) => a.weakSkills)
  );

  const strongSkillCounts = countOccurrences(strongSkillsAll);
  const weakSkillCounts = countOccurrences(weakSkillsAll);

  // ----- Chart Data -----
  const labels = filteredAnalytics.map((a) => a.filename || "N/A");

  const matchScoreData = {
    labels,
    datasets: [
      {
        label: "Match Score",
        data: filteredAnalytics.map((a) => a.matchScore ?? 0),
        borderColor: "cyan",
        backgroundColor: "rgba(0,255,255,0.2)",
        tension: 0.4,
        pointStyle: "star",
        pointRadius: 6,
      },
    ],
  };

  const skillCountData = {
    labels,
    datasets: [
      {
        label: "Strong Skills",
        data: filteredAnalytics.map((a) => a.goodSkillsCount ?? 0),
        backgroundColor: "limegreen",
      },
      {
        label: "Weak Skills",
        data: filteredAnalytics.map((a) => a.weakSkillsCount ?? 0),
        backgroundColor: "tomato",
      },
    ],
  };

  const topStrongSkillsData = {
    labels: Object.keys(strongSkillCounts),
    datasets: [
      {
        label: "Top Strong Skills",
        data: Object.values(strongSkillCounts),
        backgroundColor: Object.keys(strongSkillCounts).map(
          () => `hsl(${Math.random() * 360}, 70%, 60%)`
        ),
      },
    ],
  };

  const topWeakSkillsData = {
    labels: Object.keys(weakSkillCounts),
    datasets: [
      {
        label: "Top Weak Skills",
        data: Object.values(weakSkillCounts),
        backgroundColor: Object.keys(weakSkillCounts).map(
          () => `hsl(${Math.random() * 360}, 70%, 60%)`
        ),
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 1200, easing: "easeOutQuart" },
    plugins: {
      tooltip: {
        callbacks: {
          label: function (context: any) {
            const a = filteredAnalytics[context.dataIndex];
            return `${a.filename || "N/A"} — ${a.matchScore ?? 0}%`;
          },
        },
      },
      legend: { labels: { color: "#fff" } },
    },
    scales: {
      x: { ticks: { color: "#fff" } },
      y: { ticks: { color: "#fff" } },
    },
  };

  // ----- CSV Export -----
  const exportToCSV = () => {
    if (filteredAnalytics.length === 0) return;

    const headers = [
      "Resume",
      "Match Score",
      "Strong Skills",
      "Weak Skills",
    ];

    const rows = filteredAnalytics.map((a) => [
      a.filename || "N/A",
      a.matchScore ?? 0,
      a.strongSkills.join(", "),
      a.weakSkills.join(", "),
    ]);

    const csvContent =
      "data:text/csv;charset=utf-8," +
      [headers, ...rows].map((e) => e.join(",")).join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "resume_analytics.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div
      className={
        darkMode
          ? "bg-gradient-to-r from-gray-900 via-purple-900 to-blue-900 min-h-screen text-white"
          : "min-h-screen bg-white text-gray-900"
      }
    >
      <div className="max-w-7xl mx-auto p-6 space-y-10">
        {/* Top Controls */}
        <div className="flex flex-col md:flex-row items-center justify-between gap-4 mb-6">
          <div className="flex gap-2">
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="p-2 rounded-md text-black"
            />
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="p-2 rounded-md text-black"
            />
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setDarkMode(!darkMode)}
              className="px-3 py-1 rounded-xl bg-cyan-500 hover:bg-cyan-400 transition font-semibold flex items-center gap-1"
            >
              {darkMode ? "Light Mode" : "Dark Mode"}
            </button>

            <button
              onClick={exportToCSV}
              className="px-3 py-1 rounded-xl bg-green-500 hover:bg-green-400 transition font-semibold flex items-center gap-1"
            >
              <FaDownload /> Export CSV
            </button>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          <motion.div
            whileHover={{ scale: 1.05 }}
            className="bg-white/10 backdrop-blur-md rounded-xl shadow-lg p-6 flex flex-col items-center"
          >
            <FaFileAlt size={28} className="text-cyan-400 mb-2" />
            <span className="text-2xl font-bold">{analytics.length}</span>
            <span className="text-white/70">Resumes Uploaded</span>
          </motion.div>
          <motion.div
            whileHover={{ scale: 1.05 }}
            className="glass-card flex flex-col items-center p-6 rounded-xl shadow-lg bg-white/10 backdrop-blur-md"
          >
            <FaArrowUp size={28} className="text-green-400 mb-2" />
            <span className="text-2xl font-bold">
              {Math.round(
                filteredAnalytics.reduce(
                  (a, b) => a + (b.matchScore || 0),
                  0
                ) / Math.max(filteredAnalytics.length, 1)
              )}
              %
            </span>
            <span className="text-white/70">Avg Match Score</span>
          </motion.div>
          <motion.div
            whileHover={{ scale: 1.05 }}
            className="glass-card flex flex-col items-center p-6 rounded-xl shadow-lg bg-white/10 backdrop-blur-md"
          >
            <FaArrowUp size={28} className="text-lime-400 mb-2" />
            <span className="text-2xl font-bold">{strongSkillsAll.length}</span>
            <span className="text-white/70">Strong Skills</span>
          </motion.div>
          <motion.div
            whileHover={{ scale: 1.05 }}
            className="glass-card flex flex-col items-center p-6 rounded-xl shadow-lg bg-white/10 backdrop-blur-md"
          >
            <FaArrowDown size={28} className="text-red-400 mb-2" />
            <span className="text-2xl font-bold">{weakSkillsAll.length}</span>
            <span className="text-white/70">Weak Skills</span>
          </motion.div>
        </div>

        {/* Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <motion.div whileHover={{ scale: 1.03 }} className="glass-card h-64 p-4 rounded-xl shadow-lg bg-white/10 backdrop-blur-md">
            <h2 className="text-xl font-semibold mb-2">
              Match Score Over Time
            </h2>
            <Line data={matchScoreData} options={chartOptions} />
          </motion.div>

          <motion.div whileHover={{ scale: 1.03 }} className="glass-card h-64 p-4 rounded-xl shadow-lg bg-white/10 backdrop-blur-md">
            <h2 className="text-xl font-semibold mb-2">
              Strong vs Weak Skills
            </h2>
            <Bar data={skillCountData} options={chartOptions} />
          </motion.div>

          <motion.div whileHover={{ scale: 1.03 }} className="glass-card h-64 p-4 rounded-xl shadow-lg bg-white/10 backdrop-blur-md">
            <h2 className="text-xl font-semibold mb-2">Top Strong Skills</h2>
            <Pie data={topStrongSkillsData} options={chartOptions} />
          </motion.div>

          <motion.div whileHover={{ scale: 1.03 }} className="glass-card h-64 p-4 rounded-xl shadow-lg bg-white/10 backdrop-blur-md">
            <h2 className="text-xl font-semibold mb-2">Top Weak Skills</h2>
            <Pie data={topWeakSkillsData} options={chartOptions} />
          </motion.div>
        </div>

        {/* Table */}
        <motion.div
          whileHover={{ scale: 1.02 }}
          className="glass-card overflow-x-auto p-4 rounded-xl shadow-lg bg-white/10 backdrop-blur-md"
        >
          <h2 className="text-xl font-semibold mb-4">Resume Overview</h2>
          <table className="w-full table-auto border-collapse text-white">
            <thead className="bg-white/20">
              <tr>
                <th className="p-3 text-left">Resume</th>
                <th className="p-3 text-left">Uploaded At</th>
                <th className="p-3 text-left">Match Score</th>
                <th className="p-3 text-left">Strong Skills</th>
                <th className="p-3 text-left">Weak Skills</th>
              </tr>
            </thead>
            <tbody>
              {filteredAnalytics.map((a) => (
                <tr key={a.resumeId} className="hover:bg-white/20 transition">
                  <td className="p-2 flex items-center gap-2">
                    <FaFileAlt /> {a.filename || "N/A"}
                  </td>
                  <td className="p-2">{new Date(a.uploadedAt).toLocaleDateString()}</td>
                  <td
                    className="p-2 relative group cursor-pointer"
                    title={`Filename: ${a.filename || "N/A"}\nJob Description:\n${a.jobDescription || "N/A"}`}
                  >
                    <span className="group-hover:underline">{a.matchScore ?? 0}%</span>
                  </td>
                  <td className="p-2">{a.goodSkillsCount}</td>
                  <td className="p-2">{a.weakSkillsCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </motion.div>
      </div>
    </div>
  );
}
