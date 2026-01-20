"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useOnboardingStatus } from "@/hooks/use-api";

export default function OAuthCallbackPage() {
  const router = useRouter();
  const { data: onboardingStatus, isLoading, error } = useOnboardingStatus();

  useEffect(() => {
    if (isLoading) return;

    if (error) {
      console.error("OAuth callback error:", error);
      router.push("/");
      return;
    }

    if (onboardingStatus) {
      const isComplete = onboardingStatus.isOnboardingComplete;

      if (isComplete) {
        router.push("/dashboard");
      } else {
        router.push("/onboarding");
      }
    }
  }, [onboardingStatus, isLoading, error, router]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
        <p className="text-sm text-muted-foreground">Completing authentication...</p>
      </div>
    </div>
  );
}
