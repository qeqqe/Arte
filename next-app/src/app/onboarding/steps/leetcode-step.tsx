"use client";

import { useState } from "react";
import { Loader2, CheckCircle2, Trophy, Target, Flame } from "lucide-react";
import { useIngestLeetCode, useProfile } from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";

interface LeetCodeStepProps {
  isComplete: boolean;
  onComplete: () => void;
}

export function LeetCodeStep({ isComplete, onComplete }: LeetCodeStepProps) {
  const [username, setUsername] = useState("");
  const { mutate: ingestLeetCode, isPending, error } = useIngestLeetCode();
  const { data: profile } = useProfile();
  const [hasIngested, setHasIngested] = useState(isComplete);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) return;

    ingestLeetCode(username.trim(), {
      onSuccess: (data) => {
        if (data.success) {
          setHasIngested(true);
        }
      },
    });
  };

  if (hasIngested && profile?.leetcodeStats) {
    const stats = profile.leetcodeStats;
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-green-600">
          <CheckCircle2 className="h-5 w-5" />
          <span className="font-medium">LeetCode data imported for @{stats.username}</span>
        </div>

        <div className="space-y-4">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="p-4 rounded-lg bg-muted/50">
              <div className="text-2xl font-bold">{stats.totalSolved}</div>
              <div className="text-sm text-muted-foreground">Problems Solved</div>
            </div>
            <div className="p-4 rounded-lg bg-green-500/10">
              <div className="text-2xl font-bold text-green-600">{stats.easySolved}</div>
              <div className="text-sm text-muted-foreground">Easy</div>
            </div>
            <div className="p-4 rounded-lg bg-yellow-500/10">
              <div className="text-2xl font-bold text-yellow-600">{stats.mediumSolved}</div>
              <div className="text-sm text-muted-foreground">Medium</div>
            </div>
            <div className="p-4 rounded-lg bg-red-500/10">
              <div className="text-2xl font-bold text-red-600">{stats.hardSolved}</div>
              <div className="text-sm text-muted-foreground">Hard</div>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            {stats.ranking && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-muted/50">
                <Trophy className="h-4 w-4 text-yellow-500" />
                <div>
                  <div className="text-sm font-medium">#{stats.ranking.toLocaleString()}</div>
                  <div className="text-xs text-muted-foreground">Global Rank</div>
                </div>
              </div>
            )}
            {stats.contestRating && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-muted/50">
                <Target className="h-4 w-4 text-blue-500" />
                <div>
                  <div className="text-sm font-medium">{Math.round(stats.contestRating)}</div>
                  <div className="text-xs text-muted-foreground">Contest Rating</div>
                </div>
              </div>
            )}
            {stats.contestsAttended && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-muted/50">
                <Flame className="h-4 w-4 text-orange-500" />
                <div>
                  <div className="text-sm font-medium">{stats.contestsAttended}</div>
                  <div className="text-xs text-muted-foreground">Contests</div>
                </div>
              </div>
            )}
          </div>

          {stats.languageStats && Object.keys(stats.languageStats).length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Languages Used</div>
              <div className="flex flex-wrap gap-2">
                {Object.entries(stats.languageStats)
                  .sort(([, a], [, b]) => b - a)
                  .map(([lang, count]) => (
                    <Badge key={lang} variant="secondary">
                      {lang}: {count}
                    </Badge>
                  ))}
              </div>
            </div>
          )}
        </div>

        <Button onClick={onComplete} className="w-full">
          Continue to Resume
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <p className="text-sm text-muted-foreground">
        Enter your LeetCode username to import your problem-solving statistics, 
        contest ratings, and coding language preferences.
      </p>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="leetcode-username">LeetCode Username</Label>
          <Input
            id="leetcode-username"
            placeholder="Enter your LeetCode username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={isPending}
          />
        </div>

        {error && (
          <div className="p-3 rounded-lg bg-destructive/10 text-destructive text-sm">
            Failed to import LeetCode data. Please check your username and try again.
          </div>
        )}

        <Button
          type="submit"
          disabled={isPending || !username.trim()}
          className="w-full gap-2"
        >
          {isPending ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Importing your stats...
            </>
          ) : (
            "Import LeetCode Stats"
          )}
        </Button>
      </form>

      <div className="flex items-center gap-2">
        <div className="flex-1 h-px bg-border" />
        <span className="text-xs text-muted-foreground">or</span>
        <div className="flex-1 h-px bg-border" />
      </div>

      <Button
        variant="outline"
        onClick={onComplete}
        className="w-full"
      >
        Skip for now
      </Button>
    </div>
  );
}
