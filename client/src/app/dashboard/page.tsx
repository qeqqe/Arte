"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { 
  Loader2, 
  Moon, 
  Sun,
  LogOut,
  User as UserIcon,
  ChevronDown
} from "lucide-react";
import { useTheme } from "next-themes";
import { useUser, useOnboardingStatus } from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ProfileSection } from "./sections/profile-section";
import { JobAnalysisSection } from "./sections/job-analysis-section";
import { ComparisonHistorySection } from "./sections/comparison-history-section";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082";

export default function DashboardPage() {
  const router = useRouter();
  const { theme, setTheme } = useTheme();
  const { data: user, isLoading: userLoading, error: userError } = useUser();
  const { data: onboardingStatus, isLoading: statusLoading } = useOnboardingStatus();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true); 
  }, []);

  useEffect(() => {
    if (!userError) return;
    const status = (userError as any)?.status;
    if (status === 401) {
      router.push("/");
    }
  }, [userError, router]);

  useEffect(() => {
    if (onboardingStatus && !onboardingStatus.isOnboardingComplete) {
      router.push("/onboarding");
    }
  }, [onboardingStatus, router]);

  if (userLoading || statusLoading || !mounted) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (userError) {
    return (
      <div className="min-h-screen flex items-center justify-center flex-col gap-4">
        <p className="text-red-500">Error loading user: {(userError as any)?.message || "Unexpected error"}</p>
        <Button onClick={() => window.location.reload()}>Retry</Button>
      </div>
    );
  }

  if (!user || !onboardingStatus?.isOnboardingComplete) {
    return null;
  }

  const handleLogout = () => {
    document.cookie = "accessToken=; Max-Age=0; path=/; domain=localhost";
    document.cookie = "refreshToken=; Max-Age=0; path=/; domain=localhost";
    router.push("/");
  };

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h1 className="text-xl font-bold">Arte</h1>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            >
              {theme === "dark" ? (
                <Sun className="h-4 w-4" />
              ) : (
                <Moon className="h-4 w-4" />
              )}
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="gap-2">
                  <Avatar className="h-6 w-6">
                    <AvatarFallback className="text-xs">
                      {user.githubUsername.slice(0, 2).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  <span className="hidden sm:inline">{user.githubUsername}</span>
                  <ChevronDown className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-48">
                <DropdownMenuItem disabled>
                  <UserIcon className="mr-2 h-4 w-4" />
                  {user.email}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>
                  <LogOut className="mr-2 h-4 w-4" />
                  Sign out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 space-y-8">
        <ProfileSection />
        <JobAnalysisSection />
        <ComparisonHistorySection />
      </main>
    </div>
  );
}
