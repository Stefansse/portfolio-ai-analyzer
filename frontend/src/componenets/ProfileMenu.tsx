import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";

export default function ProfileMenu() {
  const { email, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const avatarLetter = email?.[0].toUpperCase() || "U";

  return (
    <div className="relative inline-block text-left z-50">
      {/* Avatar Button */}
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center justify-center w-10 h-10 bg-gradient-to-r from-[#1B4332]/80 via-[#2D6A4F]/80 to-[#40916C]/80 rounded-full shadow-lg hover:scale-105 transform transition"
      >
        <span className="text-white font-bold text-lg">{avatarLetter}</span>
      </button>

      {/* Dropdown */}
      {open && (
        <div className="absolute right-0 mt-2 w-52 bg-white/10 backdrop-blur-lg border border-white/20 rounded-xl shadow-2xl z-50 overflow-hidden animate-fadeIn">
          <div className="px-4 py-3 text-white text-sm">
            <p className="truncate"><strong>Email:</strong> {email || "N/A"}</p>
          </div>
          <button
            onClick={() => { logout(); setOpen(false); }}
            className="w-full text-left px-4 py-2 text-green-200 hover:text-green-400 transition rounded-b-lg font-semibold bg-transparent"
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}
