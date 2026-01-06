"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { CheckCircle2, Circle, Loader2 } from "lucide-react";
import { useOnboardingStatus, useUser } from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";
import { GitHubStep } from "./steps/github-step";
import { LeetCodeStep } from "./steps/leetcode-step";
import { ResumeStep } from "./steps/resume-step";

type Step = "github" | "leetcode" | "resume" | "complete";

const steps: { id: Step; title: string; description: string }[] = [
  { id: "github", title: "GitHub Profile", description: "Import your repositories and coding activity" },
  { id: "leetcode", title: "LeetCode Stats", description: "Connect your problem-solving history" },
  { id: "resume", title: "Resume", description: "Upload or enter your work experience" },
];

export default function OnboardingPage() {
  const router = useRouter();
  const { data: user, isLoading: userLoading, error: userError } = useUser();
  const { data: status, isLoading: statusLoading } = useOnboardingStatus();
  const [currentStep, setCurrentStep] = useState<Step>("github");

  useEffect(() => {
    if (!userError) return;
    const status = (userError as any)?.status;
    if (status === 401) {
      router.push("/");
    }
  }, [userError, router]);

  useEffect(() => {
    if (status) {
      if (status.isOnboardingComplete) {
        router.push("/dashboard");
      } else if (!status.hasGithubData) {
        setCurrentStep("github");
      } else if (!status.hasLeetcodeData) {
        setCurrentStep("leetcode");
      } else if (!status.hasResumeData) {
        setCurrentStep("resume");
      }
    }
  }, [status, router]);

  if (userLoading || statusLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!user) {
    return null;
  }

  if (userError) {
    return (
      <div className="min-h-screen flex items-center justify-center flex-col gap-4">
        <p className="text-red-500">Error loading user: {(userError as any)?.message || "Unexpected error"}</p>
        <Button onClick={() => window.location.reload()}>Retry</Button>
      </div>
    );
  }

  const completedSteps = [
    status?.hasGithubData,
    status?.hasLeetcodeData,
    status?.hasResumeData,
  ].filter(Boolean).length;

  const progressPercentage = (completedSteps / 3) * 100;

  const handleStepComplete = (step: Step) => {
    if (step === "github") {
      setCurrentStep("leetcode");
    } else if (step === "leetcode") {
      setCurrentStep("resume");
    } else if (step === "resume") {
      router.push("/dashboard");
    }
  };

  const canNavigateToStep = (step: Step): boolean => {
    if (step === "github") return true;
    if (step === "leetcode") return !!status?.hasGithubData;
    if (step === "resume") return !!status?.hasGithubData && !!status?.hasLeetcodeData;
    return false;
  };

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-3xl mx-auto space-y-8">
        <div className="text-center space-y-2">
          <h1 className="text-2xl font-bold">Welcome, {user.githubUsername}</h1>
          <p className="text-muted-foreground">
            Let&apos;s set up your profile to get personalized job matching
          </p>
        </div>

        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Setup Progress</span>
            <span className="font-medium">{completedSteps} of 3 complete</span>
          </div>
          <Progress value={progressPercentage} className="h-2" />
        </div>

        <div className="flex gap-2 overflow-x-auto pb-2">
          {steps.map((step, index) => {
            const isComplete = 
              (step.id === "github" && status?.hasGithubData) ||
              (step.id === "leetcode" && status?.hasLeetcodeData) ||
              (step.id === "resume" && status?.hasResumeData);
            const isCurrent = currentStep === step.id;
            const canNavigate = canNavigateToStep(step.id);

            return (
              <Button
                key={step.id}
                variant={isCurrent ? "default" : "outline"}
                className={cn(
                  "flex-1 min-w-[120px] h-auto py-3 px-4 flex flex-col items-start gap-1",
                  !canNavigate && "opacity-50 cursor-not-allowed"
                )}
                onClick={() => canNavigate && setCurrentStep(step.id)}
                disabled={!canNavigate}
              >
                <div className="flex items-center gap-2 w-full">
                  {isComplete ? (
                    <CheckCircle2 className="h-4 w-4 text-green-500" />
                  ) : (
                    <Circle className="h-4 w-4" />
                  )}
                  <span className="text-xs text-muted-foreground">Step {index + 1}</span>
                </div>
                <span className="text-sm font-medium">{step.title}</span>
              </Button>
            );
          })}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>
              {steps.find(s => s.id === currentStep)?.title}
            </CardTitle>
            <CardDescription>
              {steps.find(s => s.id === currentStep)?.description}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {currentStep === "github" && (
              <GitHubStep 
                isComplete={!!status?.hasGithubData}
                onComplete={() => handleStepComplete("github")} 
              />
            )}
            {currentStep === "leetcode" && (
              <LeetCodeStep 
                isComplete={!!status?.hasLeetcodeData}
                onComplete={() => handleStepComplete("leetcode")} 
              />
            )}
            {currentStep === "resume" && (
              <ResumeStep 
                isComplete={!!status?.hasResumeData}
                onComplete={() => handleStepComplete("resume")} 
              />
            )}
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
