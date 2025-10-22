export interface MatchEvaluation {
  match_score?: string;
  summary?: string;
  strengths?: string[];
  weaknesses?: string[];
}

export interface ResumeResponseDTO {
  id: number;
  filename: string;
  uploadedAt: string;
  url: string;
  extractedText?: string; // add this if backend sends it
  matchEvaluation?: MatchEvaluation;
}

export interface UserDTO {
  id?: number;
  email: string;
  password: string;
}


export interface AnalyticsDTO {
  id?: number;
  userId: number;
  resumeId: number;
  weakSkills: string[];
  strongSkills: string[];
  weakSkillsCount?: number;
  goodSkillsCount?: number;
  matchScore?: number;
  uploadedAt: string; // ISO string
  jobDescription?: string; // Text content of the job description (from @Lob)
  filename?: string;

}
