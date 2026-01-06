"use client";

import { useState } from "react";
import { 
  Github, 
  Code2, 
  FileText, 
  Star, 
  GitFork, 
  Trophy,
  Target,
  Flame,
  ChevronDown,
  ChevronUp,
  RefreshCw,
  CheckCircle2
} from "lucide-react";
import { useProfile, useProcessUser } from "@/hooks/use-api";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

export function ProfileSection() {
  const { data: profile, isLoading, refetch } = useProfile();
  const { mutate: processUser, isPending: isProcessing } = useProcessUser();
  const [expandedSection, setExpandedSection] = useState<string | null>(null);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-9 w-32" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-48" />
          ))}
        </div>
      </div>
    );
  }

  if (!profile) {
    return null;
  }

  const handleProcessProfile = () => {
    processUser(undefined, {
      onSuccess: () => {
        refetch();
      },
    });
  };

  const toggleSection = (section: string) => {
    setExpandedSection(expandedSection === section ? null : section);
  };

  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Your Profile</h2>
          <p className="text-sm text-muted-foreground">
            Overview of your technical profile and skills
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleProcessProfile}
            disabled={isProcessing}
          >
            {isProcessing ? (
              <>
                <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : profile.hasProcessedData ? (
              <>
                <RefreshCw className="mr-2 h-4 w-4" />
                Refresh Analysis
              </>
            ) : (
              <>
                <Target className="mr-2 h-4 w-4" />
                Analyze Profile
              </>
            )}
          </Button>
        </div>
      </div>

      {profile.hasProcessedData && (
        <div className="flex items-center gap-2 text-sm text-green-600">
          <CheckCircle2 className="h-4 w-4" />
          Profile analyzed and ready for job matching
        </div>
      )}

      <Tabs defaultValue="github" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="github" className="gap-2">
            <Github className="h-4 w-4" />
            <span className="hidden sm:inline">GitHub</span>
          </TabsTrigger>
          <TabsTrigger value="leetcode" className="gap-2">
            <Code2 className="h-4 w-4" />
            <span className="hidden sm:inline">LeetCode</span>
          </TabsTrigger>
          <TabsTrigger value="resume" className="gap-2">
            <FileText className="h-4 w-4" />
            <span className="hidden sm:inline">Resume</span>
          </TabsTrigger>
        </TabsList>

        <TabsContent value="github" className="mt-4">
          <GitHubProfileCard 
            stats={profile.githubStats} 
            isExpanded={expandedSection === "github"}
            onToggle={() => toggleSection("github")}
          />
        </TabsContent>

        <TabsContent value="leetcode" className="mt-4">
          <LeetCodeProfileCard 
            stats={profile.leetcodeStats}
            isExpanded={expandedSection === "leetcode"}
            onToggle={() => toggleSection("leetcode")}
          />
        </TabsContent>

        <TabsContent value="resume" className="mt-4">
          <ResumeProfileCard 
            summary={profile.resumeSummary}
            isExpanded={expandedSection === "resume"}
            onToggle={() => toggleSection("resume")}
          />
        </TabsContent>
      </Tabs>
    </section>
  );
}

interface GitHubProfileCardProps {
  stats: NonNullable<ReturnType<typeof useProfile>["data"]>["githubStats"];
  isExpanded: boolean;
  onToggle: () => void;
}

function GitHubProfileCard({ stats, isExpanded, onToggle }: GitHubProfileCardProps) {
  if (!stats) {
    return (
      <Card>
        <CardContent className="py-8 text-center text-muted-foreground">
          No GitHub data available
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg flex items-center gap-2">
            <Github className="h-5 w-5" />
            GitHub Overview
          </CardTitle>
          <Button variant="ghost" size="sm" onClick={onToggle}>
            {isExpanded ? (
              <ChevronUp className="h-4 w-4" />
            ) : (
              <ChevronDown className="h-4 w-4" />
            )}
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-3 gap-4">
          <div className="text-center p-3 rounded-lg bg-muted/50">
            <div className="flex items-center justify-center gap-1">
              <Star className="h-4 w-4 text-yellow-500" />
              <span className="text-xl font-bold">{stats.totalStars}</span>
            </div>
            <div className="text-xs text-muted-foreground">Stars</div>
          </div>
          <div className="text-center p-3 rounded-lg bg-muted/50">
            <div className="flex items-center justify-center gap-1">
              <GitFork className="h-4 w-4 text-blue-500" />
              <span className="text-xl font-bold">{stats.totalForks}</span>
            </div>
            <div className="text-xs text-muted-foreground">Forks</div>
          </div>
          <div className="text-center p-3 rounded-lg bg-muted/50">
            <span className="text-xl font-bold">{stats.totalPinnedRepos}</span>
            <div className="text-xs text-muted-foreground">Repos</div>
          </div>
        </div>

        <div className={cn("space-y-4", !isExpanded && "hidden")}>
          <Separator />
          
          {stats.languageDistribution && Object.keys(stats.languageDistribution).length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Languages</div>
              <div className="flex flex-wrap gap-2">
                {Object.entries(stats.languageDistribution)
                  .sort(([, a], [, b]) => b - a)
                  .slice(0, 8)
                  .map(([lang, count]) => (
                    <Badge key={lang} variant="secondary">
                      {lang}: {count}
                    </Badge>
                  ))}
              </div>
            </div>
          )}

          {stats.topTopics && stats.topTopics.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Topics</div>
              <div className="flex flex-wrap gap-2">
                {stats.topTopics.slice(0, 10).map((topic) => (
                  <Badge key={topic} variant="outline">
                    {topic}
                  </Badge>
                ))}
              </div>
            </div>
          )}

          {stats.pinnedRepos && stats.pinnedRepos.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Pinned Repositories</div>
              <div className="space-y-2">
                {stats.pinnedRepos.slice(0, 4).map((repo) => (
                  <a
                    key={repo.name}
                    href={repo.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="block p-3 rounded-lg bg-muted/30 hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-medium">{repo.name}</span>
                      <div className="flex items-center gap-3 text-sm text-muted-foreground">
                        {repo.primaryLanguage && (
                          <span>{repo.primaryLanguage}</span>
                        )}
                        <span className="flex items-center gap-1">
                          <Star className="h-3 w-3" />
                          {repo.stars}
                        </span>
                      </div>
                    </div>
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

interface LeetCodeProfileCardProps {
  stats: NonNullable<ReturnType<typeof useProfile>["data"]>["leetcodeStats"];
  isExpanded: boolean;
  onToggle: () => void;
}

function LeetCodeProfileCard({ stats, isExpanded, onToggle }: LeetCodeProfileCardProps) {
  if (!stats) {
    return (
      <Card>
        <CardContent className="py-8 text-center text-muted-foreground">
          No LeetCode data available
        </CardContent>
      </Card>
    );
  }

  const total = stats.easySolved + stats.mediumSolved + stats.hardSolved;
  const easyPercent = total ? (stats.easySolved / total) * 100 : 0;
  const mediumPercent = total ? (stats.mediumSolved / total) * 100 : 0;
  const hardPercent = total ? (stats.hardSolved / total) * 100 : 0;

  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg flex items-center gap-2">
            <Code2 className="h-5 w-5" />
            LeetCode Stats
            <Badge variant="secondary" className="ml-2">@{stats.username}</Badge>
          </CardTitle>
          <Button variant="ghost" size="sm" onClick={onToggle}>
            {isExpanded ? (
              <ChevronUp className="h-4 w-4" />
            ) : (
              <ChevronDown className="h-4 w-4" />
            )}
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="text-center mb-4">
          <div className="text-3xl font-bold">{stats.totalSolved}</div>
          <div className="text-sm text-muted-foreground">Problems Solved</div>
        </div>

        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-green-600">Easy: {stats.easySolved}</span>
            <span className="text-yellow-600">Medium: {stats.mediumSolved}</span>
            <span className="text-red-600">Hard: {stats.hardSolved}</span>
          </div>
          <div className="h-2 rounded-full bg-muted overflow-hidden flex">
            <div 
              className="bg-green-500 h-full" 
              style={{ width: `${easyPercent}%` }} 
            />
            <div 
              className="bg-yellow-500 h-full" 
              style={{ width: `${mediumPercent}%` }} 
            />
            <div 
              className="bg-red-500 h-full" 
              style={{ width: `${hardPercent}%` }} 
            />
          </div>
        </div>

        <div className={cn("space-y-4", !isExpanded && "hidden")}>
          <Separator />
          
          <div className="grid grid-cols-3 gap-3">
            {stats.ranking && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-muted/50">
                <Trophy className="h-4 w-4 text-yellow-500" />
                <div>
                  <div className="text-sm font-medium">#{stats.ranking.toLocaleString()}</div>
                  <div className="text-xs text-muted-foreground">Rank</div>
                </div>
              </div>
            )}
            {stats.contestRating && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-muted/50">
                <Target className="h-4 w-4 text-blue-500" />
                <div>
                  <div className="text-sm font-medium">{Math.round(stats.contestRating)}</div>
                  <div className="text-xs text-muted-foreground">Rating</div>
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

          {stats.badges && stats.badges.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Badges</div>
              <div className="flex flex-wrap gap-2">
                {stats.badges.slice(0, 6).map((badge, i) => (
                  <Badge key={i} variant="outline">
                    {badge}
                  </Badge>
                ))}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

interface ResumeProfileCardProps {
  summary: NonNullable<ReturnType<typeof useProfile>["data"]>["resumeSummary"];
  isExpanded: boolean;
  onToggle: () => void;
}

function ResumeProfileCard({ summary, isExpanded, onToggle }: ResumeProfileCardProps) {
  if (!summary) {
    return (
      <Card>
        <CardContent className="py-8 text-center text-muted-foreground">
          No resume data available
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Resume Summary
          </CardTitle>
          <Button variant="ghost" size="sm" onClick={onToggle}>
            {isExpanded ? (
              <ChevronUp className="h-4 w-4" />
            ) : (
              <ChevronDown className="h-4 w-4" />
            )}
          </Button>
        </div>
        <CardDescription>{summary.fileName} • {summary.wordCount} words</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {summary.skills && summary.skills.length > 0 && (
          <div>
            <div className="text-sm font-medium mb-2">Skills</div>
            <div className="flex flex-wrap gap-2">
              {summary.skills.slice(0, isExpanded ? 20 : 8).map((skill, i) => (
                <Badge key={i} variant="secondary">
                  {skill}
                </Badge>
              ))}
              {!isExpanded && summary.skills.length > 8 && (
                <Badge variant="outline">+{summary.skills.length - 8} more</Badge>
              )}
            </div>
          </div>
        )}

        <div className={cn("space-y-4", !isExpanded && "hidden")}>
          <Separator />

          {summary.education && summary.education.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Education</div>
              <div className="space-y-1">
                {summary.education.map((edu, i) => (
                  <div key={i} className="text-sm text-muted-foreground p-2 rounded bg-muted/30">
                    {edu}
                  </div>
                ))}
              </div>
            </div>
          )}

          {summary.experiences && summary.experiences.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Experience</div>
              <div className="space-y-1">
                {summary.experiences.slice(0, 5).map((exp, i) => (
                  <div key={i} className="text-sm text-muted-foreground p-2 rounded bg-muted/30">
                    {exp}
                  </div>
                ))}
              </div>
            </div>
          )}

          {summary.summary && (
            <div>
              <div className="text-sm font-medium mb-2">Summary</div>
              <div className="text-sm text-muted-foreground p-3 rounded bg-muted/30">
                {summary.summary}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
