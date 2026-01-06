import type {
  User,
  UserProfile,
  OnboardingStatus,
  TokenResponse,
  LeetCodeResponse,
  GitHubResponse,
  ResumeResponse,
  LinkedInJobResponse,
  ProcessUserResponse,
  ProcessJobResponse,
  CompareUserJobResponse,
  LinkedInJob,
  JobComparisonSummary,
  ApiError,
} from "@/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082";

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  headers?: Record<string, string>;
};

class ApiClient {
  private baseUrl: string;
  private refreshPromise: Promise<TokenResponse> | null = null;
  private tokenExpiresAt: number | null = null;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private getCookie(name: string): string | undefined {
    if (typeof document === "undefined") return undefined;
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(";").shift();
    return undefined;
  }

  private async checkAndRefreshToken(): Promise<void> {
    const accessToken = this.getCookie("accessToken");
    if (!accessToken) return;

    try {
      const payload = JSON.parse(atob(accessToken.split(".")[1]));
      const expiresAt = payload.exp * 1000;
      const now = Date.now();
      const fiveMinutes = 5 * 60 * 1000;

      if (expiresAt - now < fiveMinutes) {
        await this.refreshToken();
      }
    } catch {
      // Token parsing failed, will handle in request
    }
  }

  private async refreshToken(): Promise<TokenResponse> {
    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    this.refreshPromise = (async () => {
      try {
        const response = await fetch(`${this.baseUrl}/api/auth/refresh`, {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error("Failed to refresh token");
        }

        const data = await response.json();
        this.tokenExpiresAt = Date.now() + data.expiresIn * 1000;
        return data;
      } finally {
        this.refreshPromise = null;
      }
    })();

    return this.refreshPromise;
  }

  async request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
    await this.checkAndRefreshToken();

    const { method = "GET", body, headers = {} } = options;

    const requestHeaders: Record<string, string> = {
      ...headers,
    };

    if (body && !(body instanceof FormData)) {
      requestHeaders["Content-Type"] = "application/json";
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method,
      credentials: "include",
      headers: requestHeaders,
      body: body instanceof FormData ? body : body ? JSON.stringify(body) : undefined,
    });

    if (response.status === 401) {
      try {
        await this.refreshToken();
        const retryResponse = await fetch(`${this.baseUrl}${endpoint}`, {
          method,
          credentials: "include",
          headers: requestHeaders,
          body: body instanceof FormData ? body : body ? JSON.stringify(body) : undefined,
        });

        if (!retryResponse.ok) {
          const error: ApiError = await retryResponse.json().catch(() => ({
            error: "Error",
            message: "Request failed after token refresh",
          }));
          throw new ApiClientError(error.message, retryResponse.status, error);
        }

        return retryResponse.json();
      } catch (refreshError) {
        if (typeof window !== "undefined") {
          window.location.href = "/";
        }
        throw refreshError;
      }
    }

    if (!response.ok) {
      const error: ApiError = await response.json().catch(() => ({
        error: "Error",
        message: "An unexpected error occurred",
      }));
      throw new ApiClientError(error.message, response.status, error);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json();
  }

  async getCurrentUser(): Promise<User> {
    return this.request<User>("/api/auth/me");
  }

  async getUserProfile(): Promise<UserProfile> {
    return this.request<UserProfile>("/api/users/profile");
  }

  async getOnboardingStatus(): Promise<OnboardingStatus> {
    return this.request<OnboardingStatus>("/api/users/onboarding-status");
  }

  async getUserComparisons(): Promise<JobComparisonSummary[]> {
    return this.request<JobComparisonSummary[]>("/api/users/comparisons");
  }

  async getComparisonByJobId(jobId: string): Promise<Record<string, unknown>> {
    return this.request<Record<string, unknown>>(`/api/users/comparisons/${jobId}`);
  }

  async getJobByJobId(jobId: string): Promise<LinkedInJob> {
    return this.request<LinkedInJob>(`/api/users/jobs/${jobId}`);
  }

  async ingestLeetCode(leetcodeUsername: string): Promise<LeetCodeResponse> {
    return this.request<LeetCodeResponse>("/api/ingestion/leetcode", {
      method: "POST",
      body: { leetcodeUsername },
    });
  }

  async ingestGitHub(): Promise<GitHubResponse> {
    return this.request<GitHubResponse>("/api/ingestion/github");
  }

  async ingestResume(file: File): Promise<ResumeResponse> {
    const formData = new FormData();
    formData.append("file", file);

    return this.request<ResumeResponse>("/api/ingestion/resume", {
      method: "POST",
      body: formData,
    });
  }

  async ingestLinkedInJob(jobId: string): Promise<LinkedInJobResponse> {
    return this.request<LinkedInJobResponse>("/api/ingestion/linkedin", {
      method: "POST",
      body: { jobId },
    });
  }

  async processUser(): Promise<ProcessUserResponse> {
    return this.request<ProcessUserResponse>("/api/processing/user");
  }

  async processJob(jobId: string): Promise<ProcessJobResponse> {
    return this.request<ProcessJobResponse>("/api/processing/job", {
      method: "POST",
      body: { jobId },
    });
  }

  async compareUserJob(jobId: string): Promise<CompareUserJobResponse> {
    return this.request<CompareUserJobResponse>("/api/processing/compare", {
      method: "POST",
      body: { jobId },
    });
  }
}

export class ApiClientError extends Error {
  status: number;
  data: ApiError;

  constructor(message: string, status: number, data: ApiError) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.data = data;
  }
}

export const apiClient = new ApiClient(API_BASE_URL);
