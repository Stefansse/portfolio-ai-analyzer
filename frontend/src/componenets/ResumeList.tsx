import React, { useEffect, useState } from "react";
import { resumeApi } from "../api/axios";

export default function ResumeList() {
  const [resumes, setResumes] = useState<any[]>([]);

  useEffect(() => {
    resumeApi.get("/").then((res) => setResumes(res.data));
  }, []);

  const handleDelete = async (id: number) => {
    await resumeApi.delete(`/${id}`);
    setResumes(resumes.filter((r) => r.id !== id));
  };

  const handleDownload = async (id: number) => {
    const res = await resumeApi.get(`/${id}/download`);
    window.open(res.data.downloadUrl, "_blank");
  };

  return (
    <div className="mt-8">
      <h2 className="text-lg font-semibold mb-3">Uploaded Resumes</h2>
      <ul className="space-y-3">
        {resumes.map((r) => (
          <li
            key={r.id}
            className="flex justify-between items-center border p-3 rounded-lg"
          >
            <span>{r.filename}</span>
            <div className="flex gap-3">
              <button
                onClick={() => handleDownload(r.id)}
                className="text-blue-600 hover:underline"
              >
                Download
              </button>
              <button
                onClick={() => handleDelete(r.id)}
                className="text-red-600 hover:underline"
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
