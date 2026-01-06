export interface User {
  id: string;
  email: string;
  githubUsername: string;
  createdAt: string;
}

export interface RepoSummary {
  name: string;
  url: string;
  stars: number;
  forks: number;
  primaryLanguage: string | null;
}

export interface GitHubStats {
  totalStars: number;
  totalForks: number;
  totalPinnedRepos: number;
  pinnedRepos: RepoSummary[];
  languageDistribution: Record<string, number>;
  topTopics: string[];
  lastSynced: string | null;
}

export interface RecentSubmission {
  title: string;
  titleSlug: string;
  language: string;
  timestamp: number;
}

export interface LeetCodeStats {
  username: string;
  ranking: number | null;
  reputation: number | null;
  starRating: number | null;
  aboutMe: string | null;
  totalSolved: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  contestsAttended: number | null;
  contestRating: number | null;
  globalRanking: number | null;
  topPercentage: number | null;
  languageStats: Record<string, number>;
  badges: string[];
  activeBadge: string | null;
  recentSubmissions: RecentSubmission[];
}

export interface ResumeSummary {
  fileName: string;
  fileHash: string;
  wordCount: number;
  processedAt: string;
  rawText: string | null;
  skills: string[];
  experiences: string[];
  education: string[];
  summary: string | null;
}

export interface UserProfile {
  userId: string;
  email: string;
  githubUsername: string;
  githubStats: GitHubStats | null;
  leetcodeStats: LeetCodeStats | null;
  resumeSummary: ResumeSummary | null;
  hasProcessedData: boolean;
  processedAt: string | null;
  lastIngestedAt: string | null;
}

export interface OnboardingStatus {
  hasGithubData: boolean;
  hasLeetcodeData: boolean;
  hasResumeData: boolean;
  isOnboardingComplete: boolean;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface WorkExperience {
  company: string;
  role: string;
  duration: string;
  technologies: string[];
  achievements: string[];
}

export interface ProcessedUserData {
  userId: string;
  technicalSkills: string[];
  softSkills: string[];
  workExperiences: WorkExperience[];
  certifications: string[];
  education: string[];
  yearsOfExperience: number;
  programmingLanguages: string[];
  frameworks: string[];
  tools: string[];
  careerLevel: string;
  domains: string[];
}

export interface ProcessedJobData {
  jobId: string;
  jobTitle: string;
  company: string;
  requiredSkills: string[];
  preferredSkills: string[];
  minYearsExperience: number;
  maxYearsExperience: number;
  requiredEducation: string[];
  programmingLanguages: string[];
  frameworks: string[];
  tools: string[];
  careerLevel: string;
  domains: string[];
  responsibilities: string[];
}

export interface SkillGap {
  skillName: string;
  importance: string;
  suggestion: string;
}

export interface UserJobComparison {
  userId: string;
  jobId: string;
  overallMatchScore: number;
  skillsMatchScore: number;
  experienceMatchScore: number;
  educationMatchScore: number;
  skillGaps: SkillGap[];
  strengths: string[];
  recommendations: string[];
  fitAssessment: string;
}

export interface ProcessUserResponse {
  success: boolean;
  message: string;
  processedData: ProcessedUserData | null;
}

export interface ProcessJobResponse {
  success: boolean;
  message: string;
  processedData: ProcessedJobData | null;
}

export interface CompareUserJobResponse {
  success: boolean;
  message: string;
  comparison: UserJobComparison | null;
}

export interface LinkedInJob {
  id: string;
  jobId: string;
  rawContent: string;
  processedJobData: ProcessedJobData | null;
  isProcessed: boolean;
  createdAt: string;
}

export interface JobComparisonSummary {
  id: string;
  jobId: string;
  jobTitle: string | null;
  company: string | null;
  matchScore: number;
  comparedAt: string;
}

export interface LeetCodeResponse {
  success: boolean;
  message: string;
  problemsSolved: number;
}

export interface GitHubResponse {
  success: boolean;
  message: string;
  reposProcessed: number;
  repoNames: string[];
}

export interface ResumeResponse {
  success: boolean;
  message: string;
  wordCount: number;
}

export interface LinkedInJobResponse {
  success: boolean;
  message: string;
}

export interface ApiError {
  error: string;
  message: string;
}
