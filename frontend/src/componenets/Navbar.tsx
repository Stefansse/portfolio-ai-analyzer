import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { FaUserCircle } from "react-icons/fa";
import logo from "../assets/ailogo.png"; // import your logo

export default function Navbar() {
  const { token, email, logout } = useAuth();
  const [open, setOpen] = useState(false);

  const avatarLetter = email?.[0].toUpperCase() || "U";

  return (
    <nav className="w-full bg-gradient-to-r from-green-300 via-blue-300 to-green-400 shadow-lg relative z-50 flex items-center justify-between px-6 py-3 text-white">
      
      {/* Logo + Title */}
      <div className="flex items-center gap-3">
        <img src={logo} alt="Logo" className="w-10 h-10 object-contain" />
        <span className="text-white font-extrabold text-xl tracking-wide drop-shadow-md">
          AI Resume Analyzer
        </span>
      </div>

      {/* Links */}
      <div className="flex gap-6 mr-4">
        {["Home", "Dashboard", "History", "Compare"].map((item) => (
          <a
            key={item}
            href={item === "Home" ? "/" : `/${item.toLowerCase()}`}
            style={{
              textDecoration: "none",
              color: "rgba(255,255,255,0.9)",
              fontWeight: 600,
              padding: "0.25rem 0.75rem",
              transition: "color 0.3s ease",
              cursor: "pointer",
            }}
            onMouseEnter={(e) => (e.currentTarget.style.color = "white")}
            onMouseLeave={(e) =>
              (e.currentTarget.style.color = "rgba(255,255,255,0.9)")
            }
          >
            {item}
          </a>
        ))}
      </div>

      {/* Profile / Login */}
      <div>
        {token ? (
          <div className="relative inline-block text-left z-50">
            <button
              onClick={() => setOpen(!open)}
              className="flex items-center justify-center w-10 h-10 bg-gradient-to-r from-green-200 via-blue-200 to-green-300 rounded-full shadow-lg hover:scale-110 transform transition"
              aria-haspopup="true"
              aria-expanded={open}
            >
              <span className="text-white font-bold text-lg">{avatarLetter}</span>
            </button>

            {open && (
              <div className="absolute right-0 mt-2 w-48 bg-green-200/90 backdrop-blur-md border border-white/20 rounded-2xl shadow-2xl z-50">
                <div className="px-4 py-2 text-sm text-white">
                  <p className="truncate">
                    <strong>Email:</strong> {email || "N/A"}
                  </p>
                </div>
                <button
                  onClick={() => { logout(); setOpen(false); }}
                  className="block w-full text-left px-4 py-2 hover:bg-blue-300/70 hover:text-white rounded-b-lg transition font-medium text-white"
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        ) : (
          <button
            onClick={() => window.location.href = "/auth"}
            className="flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-r from-green-200 via-blue-200 to-green-300 hover:scale-110 transition transform shadow-lg"
            aria-label="Sign in"
          >
            <FaUserCircle size={36} className="text-white" />
          </button>
        )}
      </div>
    </nav>
  );
}
