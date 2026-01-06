"use client";

import { Github, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082";

export default function LandingPage() {
  const handleLogin = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/github`;
  };

  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-8">
      <div className="max-w-2xl text-center space-y-8">
        <div className="space-y-4">
          <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
            Arte
          </h1>
          <p className="text-lg text-muted-foreground">
            Understand your skills, analyze job requirements, and discover where you fit.
          </p>
        </div>

        <div className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Connect your GitHub to get started. We&apos;ll analyze your profile, 
            LeetCode stats, and resume to build your technical profile.
          </p>
          
          <Button 
            size="lg" 
            onClick={handleLogin}
            className="gap-2"
          >
            <Github className="h-5 w-5" />
            Continue with GitHub
            <ArrowRight className="h-4 w-4" />
          </Button>
        </div>

        <div className="pt-8 border-t border-border">
          <p className="text-xs text-muted-foreground">
            By continuing, you agree to share your GitHub profile data for analysis.
          </p>
        </div>
      </div>
    </main>
  );
}
