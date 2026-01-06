import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import type {
  UserProfile,
  OnboardingStatus,
  JobComparisonSummary,
  LinkedInJob,
  LeetCodeResponse,
  GitHubResponse,
  ResumeResponse,
  LinkedInJobResponse,
  ProcessUserResponse,
  ProcessJobResponse,
  CompareUserJobResponse,
  User,
} from "@/types";

export const queryKeys = {
  user: ["user"] as const,
  profile: ["profile"] as const,
  onboardingStatus: ["onboarding-status"] as const,
  comparisons: ["comparisons"] as const,
  comparison: (jobId: string) => ["comparison", jobId] as const,
  job: (jobId: string) => ["job", jobId] as const,
};

export function useUser() {
  return useQuery<User>({
    queryKey: queryKeys.user,
    queryFn: () => apiClient.getCurrentUser(),
    retry: false,
  });
}

export function useProfile() {
  return useQuery<UserProfile>({
    queryKey: queryKeys.profile,
    queryFn: () => apiClient.getUserProfile(),
  });
}

export function useOnboardingStatus() {
  return useQuery<OnboardingStatus>({
    queryKey: queryKeys.onboardingStatus,
    queryFn: () => apiClient.getOnboardingStatus(),
  });
}

export function useComparisons() {
  return useQuery<JobComparisonSummary[]>({
    queryKey: queryKeys.comparisons,
    queryFn: () => apiClient.getUserComparisons(),
  });
}

export function useComparison(jobId: string) {
  return useQuery<Record<string, unknown>>({
    queryKey: queryKeys.comparison(jobId),
    queryFn: () => apiClient.getComparisonByJobId(jobId),
    enabled: !!jobId,
  });
}

export function useJob(jobId: string) {
  return useQuery<LinkedInJob>({
    queryKey: queryKeys.job(jobId),
    queryFn: () => apiClient.getJobByJobId(jobId),
    enabled: !!jobId,
  });
}

export function useIngestLeetCode() {
  const queryClient = useQueryClient();

  return useMutation<LeetCodeResponse, Error, string>({
    mutationFn: (leetcodeUsername: string) =>
      apiClient.ingestLeetCode(leetcodeUsername),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.onboardingStatus });
    },
  });
}

export function useIngestGitHub() {
  const queryClient = useQueryClient();

  return useMutation<GitHubResponse, Error>({
    mutationFn: () => apiClient.ingestGitHub(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.onboardingStatus });
    },
  });
}

export function useIngestResume() {
  const queryClient = useQueryClient();

  return useMutation<ResumeResponse, Error, File>({
    mutationFn: (file: File) => apiClient.ingestResume(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.onboardingStatus });
    },
  });
}

export function useIngestLinkedInJob() {
  return useMutation<LinkedInJobResponse, Error, string>({
    mutationFn: (jobId: string) => apiClient.ingestLinkedInJob(jobId),
  });
}

export function useProcessUser() {
  const queryClient = useQueryClient();

  return useMutation<ProcessUserResponse, Error>({
    mutationFn: () => apiClient.processUser(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.profile });
    },
  });
}

export function useProcessJob() {
  return useMutation<ProcessJobResponse, Error, string>({
    mutationFn: (jobId: string) => apiClient.processJob(jobId),
  });
}

export function useCompareUserJob() {
  const queryClient = useQueryClient();

  return useMutation<CompareUserJobResponse, Error, string>({
    mutationFn: (jobId: string) => apiClient.compareUserJob(jobId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comparisons });
    },
  });
}
