import React, { useState, useEffect } from "react";
import { userApi } from "../api/axios";
import { useAuth } from "../context/AuthContext";
import GoogleLogo from "../assets/google-logo.jpg";

export default function AuthPage() {
  const { setAuth } = useAuth(); // <-- stores token + userId + email from JWT
  const [step, setStep] = useState<"login" | "register" | "verify">("login");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  // Handle OAuth or token from URL
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");
    const userId = params.get("userId"); // backend should send userId if OAuth
    if (token && userId) {
      setAuth(token, userId);
      window.location.href = "/";
    }
  }, [setAuth]);

  // ---------------- LOGIN ----------------
  const handleLogin = async () => {
    if (!email || !password) return setError("Enter email and password.");
    setLoading(true); setError(null);

    try {
      const response = await userApi.post<{
        token: string;
        userId: number;
        email: string;
      }>("/api/users/login", { email, password });

      const { token, userId } = response.data;

      setAuth(token, userId.toString()); // store token + userId
      window.location.href = "/";
    } catch (err: any) {
      setError(err.response?.data?.error || "Login failed.");
    } finally {
      setLoading(false);
    }
  };

  // ---------------- REGISTER ----------------
  const handleRegister = async () => {
    if (!fullName || !email || !password) return setError("Enter full name, email, and password.");
    setLoading(true); setError(null);

    try {
      await userApi.post("/api/users/register", { fullName, email, password });
      setStep("verify");
      setInfo(`A verification code has been sent to ${email}`);
    } catch (err: any) {
      setError(err.response?.data?.error || "Registration failed.");
    } finally {
      setLoading(false);
    }
  };

  // ---------------- VERIFY EMAIL ----------------
  const handleVerify = async () => {
    if (!code) return setError("Enter the verification code.");
    setLoading(true); setError(null);

    try {
      await userApi.post("/api/users/verify-code", { email, code });
      setInfo("Email verified! You can now log in.");
      setStep("login");
      setPassword(""); setCode(""); setFullName("");
    } catch (err: any) {
      setError(err.response?.data || "Verification failed.");
    } finally {
      setLoading(false);
    }
  };

  // ---------------- JSX ----------------
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-tr from-[#008080] via-[#00BFA6] to-[#00E5FF]">
      <div className="relative w-full max-w-md p-8 bg-white/10 backdrop-blur-md border border-white/20 rounded-3xl shadow-2xl text-white">
        <h1 className="text-3xl font-extrabold mb-6 text-center tracking-wide drop-shadow-lg">
          {step === "login" ? "Login" : step === "register" ? "Register" : "Verify Email"}
        </h1>

        {(step === "login" || step === "register") && (
          <>
            {step === "register" && (
              <input
                type="text"
                placeholder="Full Name"
                value={fullName}
                onChange={e => setFullName(e.target.value)}
                className="w-full mb-3 p-3 rounded-xl border border-white/30 bg-white/10 text-white placeholder-white focus:outline-none focus:ring-2 focus:ring-[#00BFA6] transition"
              />
            )}
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full mb-3 p-3 rounded-xl border border-white/30 bg-white/10 text-white placeholder-white focus:outline-none focus:ring-2 focus:ring-[#00BFA6] transition"
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full mb-4 p-3 rounded-xl border border-white/30 bg-white/10 text-white placeholder-white focus:outline-none focus:ring-2 focus:ring-[#00BFA6] transition"
            />
          </>
        )}

        {step === "verify" && (
          <>
            <p className="mb-2 text-white/80">{info}</p>
            <input
              type="text"
              placeholder="Verification Code"
              value={code}
              onChange={e => setCode(e.target.value)}
              className="w-full mb-4 p-3 rounded-xl border border-white/30 bg-white/10 text-white placeholder-white focus:outline-none focus:ring-2 focus:ring-[#00BFA6] transition"
            />
          </>
        )}

        {error && <p className="text-red-400 mb-2 text-center bg-white/10 backdrop-blur-md rounded-xl px-4 py-2 shadow-lg">{error}</p>}
        {info && step !== "verify" && <p className="text-white mb-2 text-center bg-white/10 backdrop-blur-md rounded-xl px-4 py-2 shadow-lg animate-pulse">{info}</p>}

        <button
          onClick={step === "login" ? handleLogin : step === "register" ? handleRegister : handleVerify}
          disabled={loading}
          className="w-full py-3 mb-4 rounded-xl bg-gradient-to-r from-[#008080]/70 via-[#00BFA6]/70 to-[#00E5FF]/70 text-white font-semibold hover:scale-105 transition transform shadow-lg disabled:opacity-50"
        >
          {loading ? (step === "login" ? "Logging in..." : step === "register" ? "Registering..." : "Verifying...") 
            : step === "login" ? "Login" : step === "register" ? "Register" : "Verify"}
        </button>

        {step === "login" && (
          <button
            onClick={() => window.location.href = "http://localhost:8081/oauth2/authorization/google"}
            className="w-full py-3 mb-4 rounded-xl bg-gradient-to-r from-[#008080]/70 via-[#00BFA6]/70 to-[#00E5FF]/70 text-white font-semibold hover:scale-105 transition flex items-center justify-center shadow-lg"
          >
            <img src={GoogleLogo} alt="Google" className="w-6 h-6 mr-3"/>
          </button>
        )}

        <p className="mt-4 text-center">
          {step === "login" ? (
            <>Don't have an account?{" "}
              <button onClick={() => { setStep("register"); setError(null); setInfo(null); }}
                className="ml-2 px-4 py-2 rounded-xl bg-gradient-to-r from-[#008080]/70 via-[#00BFA6]/70 to-[#00E5FF]/70 text-white font-semibold hover:scale-105 transition transform shadow-lg"
              >Sign Up</button>
            </>
          ) : (
            <>Already have an account?{" "}
              <button onClick={() => { setStep("login"); setError(null); setInfo(null); }}
                className="ml-2 px-4 py-2 rounded-xl bg-gradient-to-r from-[#008080]/70 via-[#00BFA6]/70 to-[#00E5FF]/70 text-white font-semibold hover:scale-105 transition transform shadow-lg"
              >Sign In</button>
            </>
          )}
        </p>

        <div className="absolute -top-10 -left-10 w-32 h-32 bg-[#00BFA6]/30 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute -bottom-12 -right-12 w-48 h-48 bg-[#00E5FF]/30 rounded-full blur-3xl animate-pulse delay-2000"></div>
      </div>
    </div>
  );
}
