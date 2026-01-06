"use client";

import { useState } from "react";
import { Github, Loader2, CheckCircle2, ExternalLink } from "lucide-react";
import { useIngestGitHub, useProfile } from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

interface GitHubStepProps {
  isComplete: boolean;
  onComplete: () => void;
}

export function GitHubStep({ isComplete, onComplete }: GitHubStepProps) {
  const { mutate: ingestGitHub, isPending, error } = useIngestGitHub();
  const { data: profile } = useProfile();
  const [hasIngested, setHasIngested] = useState(isComplete);

  const handleIngest = () => {
    ingestGitHub(undefined, {
      onSuccess: (data) => {
        if (data.success) {
          setHasIngested(true);
        }
      },
    });
  };

  if (hasIngested && profile?.githubStats) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-green-600">
          <CheckCircle2 className="h-5 w-5" />
          <span className="font-medium">GitHub data imported successfully</span>
        </div>

        <div className="space-y-4">
          <div className="grid grid-cols-3 gap-4">
            <div className="p-4 rounded-lg bg-muted/50">
              <div className="text-2xl font-bold">{profile.githubStats.totalStars}</div>
              <div className="text-sm text-muted-foreground">Total Stars</div>
            </div>
            <div className="p-4 rounded-lg bg-muted/50">
              <div className="text-2xl font-bold">{profile.githubStats.totalForks}</div>
              <div className="text-sm text-muted-foreground">Total Forks</div>
            </div>
            <div className="p-4 rounded-lg bg-muted/50">
              <div className="text-2xl font-bold">{profile.githubStats.totalPinnedRepos}</div>
              <div className="text-sm text-muted-foreground">Pinned Repos</div>
            </div>
          </div>

          {profile.githubStats.languageDistribution && (
            <div>
              <div className="text-sm font-medium mb-2">Languages</div>
              <div className="flex flex-wrap gap-2">
                {Object.entries(profile.githubStats.languageDistribution)
                  .sort(([, a], [, b]) => b - a)
                  .slice(0, 6)
                  .map(([lang, count]) => (
                    <Badge key={lang} variant="secondary">
                      {lang}: {count}
                    </Badge>
                  ))}
              </div>
            </div>
          )}

          {profile.githubStats.topTopics && profile.githubStats.topTopics.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Top Topics</div>
              <div className="flex flex-wrap gap-2">
                {profile.githubStats.topTopics.slice(0, 8).map((topic) => (
                  <Badge key={topic} variant="outline">
                    {topic}
                  </Badge>
                ))}
              </div>
            </div>
          )}
        </div>

        <Button onClick={onComplete} className="w-full">
          Continue to LeetCode
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <p className="text-sm text-muted-foreground">
          We&apos;ll analyze your public repositories, starred projects, and coding activity 
          to understand your technical skills and experience.
        </p>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <ExternalLink className="h-4 w-4" />
          <span>Only public repository data will be accessed</span>
        </div>
      </div>

      {error && (
        <div className="p-3 rounded-lg bg-destructive/10 text-destructive text-sm">
          Failed to import GitHub data. Please try again.
        </div>
      )}

      <Button
        onClick={handleIngest}
        disabled={isPending}
        className="w-full gap-2"
      >
        {isPending ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            Importing your data...
          </>
        ) : (
          <>
            <Github className="h-4 w-4" />
            Import GitHub Data
          </>
        )}
      </Button>
    </div>
  );
}
