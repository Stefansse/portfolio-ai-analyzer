import { Routes, Route, Navigate, Link } from "react-router-dom";
import AuthPage from "./pages/AuthPage";
import ResumeUploadPage from "./pages/ResumeUploadPage";
import ProfileMenu from "./componenets/ProfileMenu";
import { useAuth } from "./context/AuthContext";
import { FaUserCircle, FaGithub, FaLinkedin, FaTwitter } from "react-icons/fa";
import ResumeHistoryPage from "./pages/ResumeHistoryPage";
import Dashboard from "./componenets/Dashboard";
import ComparisonPage from "./pages/ComparisonPage";
import logo from "./assets/ailogo.png";

export default function App() {
  const { token } = useAuth();

  return (
    <div className="min-h-screen min-w-full bg-gradient-to-r from-cyan-700 via-cyan-600 to-teal-700 flex flex-col text-white">
      {/* Navbar */}
      <div
        className="flex justify-between items-center h-16 px-6 relative z-50 backdrop-blur-lg bg-white/5 shadow-md"
      >
        {/* Logo (absolute position) */}
        <img
          src={logo}
          alt="ResumeAI Logo"
          style={{
            position: "absolute",
            left: "-2px",
            height: "100%",
            objectFit: "contain",
            zIndex: 60,
          }}
        />

        {/* Navigation Links */}
        <div className="flex gap-6 font-semibold text-lg mx-auto">
          {[
            { name: "Home", path: "/" },
            { name: "Dashboard", path: "/dashboard" },
            { name: "History", path: "/history" },
            { name: "Compare", path: "/compare" },
          ].map(({ name, path }) => (
            <Link
              key={name}
              to={path}
              className="transition-colors duration-300 font-semibold"
              style={{ color: "rgba(255,255,255,0.9)", textDecoration: "none" }}
              onMouseEnter={(e) => (e.currentTarget.style.color = "#ffffff")}
              onMouseLeave={(e) =>
                (e.currentTarget.style.color = "rgba(255,255,255,0.9)")
              }
            >
              {name}
            </Link>
          ))}
        </div>

        {/* Profile/Login */}
        {token ? (
          <ProfileMenu />
        ) : (
          <button
            onClick={() => (window.location.href = "/auth")}
            className="flex items-center justify-center w-12 h-12 rounded-full bg-cyan-600/30 backdrop-blur-md shadow-lg transition transform hover:scale-110 hover:bg-cyan-500/40"
          >
            <FaUserCircle size={36} className="text-white" />
          </button>
        )}
      </div>

      {/* Main Routes */}
      <div className="flex-1">
        <Routes>
          <Route path="/auth" element={<AuthPage />} />
          <Route path="/" element={<ResumeUploadPage />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/compare" element={<ComparisonPage />} />
          <Route path="/history" element={<ResumeHistoryPage />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>

      {/* Footer */}
      <footer className="bg-white/10 backdrop-blur-md mt-10 text-white py-8 px-6 border-t border-white/20 shadow-[0_-4px_30px_rgba(0,0,0,0.2)]">
        <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Brand Section */}
          <div>
            <h2 className="text-2xl font-bold tracking-wide mb-2">
              Resume<span className="text-cyan-300">AI</span>
            </h2>
            <p className="text-white/70 text-sm leading-relaxed">
              Empower your career journey with AI-driven resume analysis.
              Get personalized insights and professional feedback instantly.
            </p>
          </div>

          {/* Explore Features */}
          <div className="flex flex-col gap-2 md:items-center">
            <h3 className="font-semibold text-lg mb-2">Explore Features</h3>
            <Link
              to="/upload"
              className="no-underline hover:no-underline decoration-none hover:text-cyan-300 transition-colors duration-300"
            >
              AI Resume Review
            </Link>
            <Link
              to="/insights"
              className="no-underline hover:no-underline decoration-none hover:text-cyan-300 transition-colors duration-300"
            >
              Skill Insights
            </Link>
            <Link
              to="/jobfit"
              className="no-underline hover:no-underline decoration-none hover:text-cyan-300 transition-colors duration-300"
            >
              Job Fit Analyzer
            </Link>
            <Link
              to="/career-paths"
              className="no-underline hover:no-underline decoration-none hover:text-cyan-300 transition-colors duration-300"
            >
              Career Recommendations
            </Link>
          </div>

          {/* Social Media */}
          <div className="flex flex-col gap-3 md:items-end">
            <h3 className="font-semibold text-lg mb-2">Connect With Us</h3>
            <div className="flex gap-4">
              <a
                href="https://github.com/"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-full bg-white/10 hover:bg-cyan-500/30 transition-transform transform hover:scale-110 no-underline"
              >
                <FaGithub size={20} />
              </a>
              <a
                href="https://linkedin.com/"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-full bg-white/10 hover:bg-cyan-500/30 transition-transform transform hover:scale-110 no-underline"
              >
                <FaLinkedin size={20} />
              </a>
              <a
                href="https://twitter.com/"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-full bg-white/10 hover:bg-cyan-500/30 transition-transform transform hover:scale-110 no-underline"
              >
                <FaTwitter size={20} />
              </a>
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="border-t border-white/20 mt-6 pt-4 text-center text-white/70 text-sm">
          © {new Date().getFullYear()} ResumeAI — Crafted with ❤️ for professionals
        </div>
      </footer>
    </div>
  );
}
