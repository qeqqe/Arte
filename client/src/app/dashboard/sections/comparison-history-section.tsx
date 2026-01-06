"use client";

import { useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { useComparisons, useJob, useComparison } from "@/hooks/use-api";
import { 
  History, 
  ChevronRight, 
  Briefcase, 
  Building2, 
  Calendar,
  Target,
  ExternalLink,
  TrendingUp,
  TrendingDown,
  CheckCircle2,
  AlertTriangle,
  Lightbulb,
  GraduationCap,
  Wrench,
  Loader2
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import type { JobComparisonSummary, UserJobComparison, SkillGap } from "@/types";

export function ComparisonHistorySection() {
  const { data: comparisons, isLoading, error } = useComparisons();
  const [selectedComparison, setSelectedComparison] = useState<JobComparisonSummary | null>(null);

  if (error) {
    return (
      <section className="space-y-4">
        <div>
          <h2 className="text-2xl font-bold">Comparison History</h2>
          <p className="text-sm text-muted-foreground">
            Review your previous job comparisons
          </p>
        </div>
        <Card>
          <CardContent className="py-8 text-center text-muted-foreground">
            Failed to load comparison history
          </CardContent>
        </Card>
      </section>
    );
  }

  return (
    <section className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold">Comparison History</h2>
        <p className="text-sm text-muted-foreground">
          Review your previous job comparisons
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            <History className="h-5 w-5" />
            Past Comparisons
          </CardTitle>
          <CardDescription>
            {comparisons?.length || 0} job comparisons performed
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center gap-4 p-4 rounded-lg border">
                  <Skeleton className="h-12 w-12 rounded-full" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-48" />
                    <Skeleton className="h-3 w-32" />
                  </div>
                  <Skeleton className="h-8 w-20" />
                </div>
              ))}
            </div>
          ) : comparisons && comparisons.length > 0 ? (
            <ScrollArea className="max-h-[400px]">
              <div className="space-y-2 pr-4">
                {comparisons.map((comparison) => (
                  <ComparisonCard
                    key={comparison.jobId}
                    comparison={comparison}
                    onView={() => setSelectedComparison(comparison)}
                  />
                ))}
              </div>
            </ScrollArea>
          ) : (
            <div className="py-12 text-center space-y-3">
              <div className="mx-auto w-12 h-12 rounded-full bg-muted flex items-center justify-center">
                <Target className="h-6 w-6 text-muted-foreground" />
              </div>
              <div>
                <p className="font-medium">No comparisons yet</p>
                <p className="text-sm text-muted-foreground">
                  Analyze a job posting above to create your first comparison
                </p>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {selectedComparison && (
        <ComparisonDetailDialog
          comparison={selectedComparison}
          open={!!selectedComparison}
          onClose={() => setSelectedComparison(null)}
        />
      )}
    </section>
  );
}

interface ComparisonCardProps {
  comparison: JobComparisonSummary;
  onView: () => void;
}

function ComparisonCard({ comparison, onView }: ComparisonCardProps) {
  const getScoreColor = (score: number) => {
    if (score >= 80) return "bg-green-500";
    if (score >= 60) return "bg-yellow-500";
    return "bg-red-500";
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: date.getFullYear() !== new Date().getFullYear() ? "numeric" : undefined,
    });
  };

  return (
    <button
      onClick={onView}
      className="w-full text-left p-4 rounded-lg border hover:bg-muted/50 transition-colors group"
    >
      <div className="flex items-center gap-4">
        <div className="relative shrink-0">
          <div 
            className={`w-12 h-12 rounded-full flex items-center justify-center ${getScoreColor(comparison.matchScore)}`}
          >
            <span className="text-white font-bold text-sm">
              {comparison.matchScore}%
            </span>
          </div>
        </div>

        <div className="flex-1 min-w-0">
          <div className="font-medium truncate">{comparison.jobTitle || "Job Posting"}</div>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Building2 className="h-3.5 w-3.5" />
            <span className="truncate">{comparison.company || "Unknown Company"}</span>
          </div>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          <div className="text-right hidden sm:block">
            <div className="flex items-center gap-1 text-xs text-muted-foreground">
              <Calendar className="h-3 w-3" />
              {formatDate(comparison.comparedAt)}
            </div>
          </div>
          <ChevronRight className="h-5 w-5 text-muted-foreground group-hover:text-foreground transition-colors" />
        </div>
      </div>
    </button>
  );
}

interface ComparisonDetailDialogProps {
  comparison: JobComparisonSummary;
  open: boolean;
  onClose: () => void;
}

function ComparisonDetailDialog({ comparison, open, onClose }: ComparisonDetailDialogProps) {
  const { data: fullComparison, isLoading: comparisonLoading } = useComparison(comparison.jobId);
  const { data: jobData } = useJob(comparison.jobId);

  // Cast the full comparison data
  const comparisonData = fullComparison as UserJobComparison | undefined;

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600";
    if (score >= 60) return "text-yellow-600";
    return "text-red-600";
  };

  const getScoreBgColor = (score: number) => {
    if (score >= 80) return "bg-green-500";
    if (score >= 60) return "bg-yellow-500";
    return "bg-red-500";
  };

  const getImportanceBadgeColor = (importance: string) => {
    const imp = importance.toLowerCase();
    if (imp === "high" || imp === "critical") return "destructive";
    if (imp === "medium") return "secondary";
    return "outline";
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-w-3xl max-h-[90vh]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Briefcase className="h-5 w-5" />
            {comparison.jobTitle || "Job Posting"}
          </DialogTitle>
          <DialogDescription className="flex items-center gap-2">
            <Building2 className="h-4 w-4" />
            {comparison.company || "Unknown Company"}
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[70vh]">
          {comparisonLoading ? (
            <div className="py-12 flex items-center justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : comparisonData ? (
            <div className="space-y-6 pr-4">
              {/* Overall Score */}
              <div className="text-center py-4">
                <div className="text-sm text-muted-foreground mb-2">Overall Match</div>
                <div className={`text-5xl font-bold ${getScoreColor(comparisonData.overallMatchScore)}`}>
                  {comparisonData.overallMatchScore}%
                </div>
              </div>

              {/* Score Breakdown */}
              <div className="grid grid-cols-3 gap-3">
                <ScoreCard 
                  label="Skills" 
                  score={comparisonData.skillsMatchScore} 
                  icon={<Wrench className="h-4 w-4" />}
                />
                <ScoreCard 
                  label="Experience" 
                  score={comparisonData.experienceMatchScore} 
                  icon={<TrendingUp className="h-4 w-4" />}
                />
                <ScoreCard 
                  label="Education" 
                  score={comparisonData.educationMatchScore} 
                  icon={<GraduationCap className="h-4 w-4" />}
                />
              </div>

              <Separator />

              {/* Fit Assessment */}
              {comparisonData.fitAssessment && (
                <div className="space-y-2">
                  <h4 className="font-semibold flex items-center gap-2">
                    <Target className="h-4 w-4 text-primary" />
                    Fit Assessment
                  </h4>
                  <p className="text-sm text-muted-foreground leading-relaxed">
                    {comparisonData.fitAssessment}
                  </p>
                </div>
              )}

              {/* Strengths */}
              {comparisonData.strengths && comparisonData.strengths.length > 0 && (
                <div className="space-y-3">
                  <h4 className="font-semibold flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-green-600" />
                    Your Strengths ({comparisonData.strengths.length})
                  </h4>
                  <div className="grid gap-2">
                    {comparisonData.strengths.map((strength, index) => (
                      <div 
                        key={index}
                        className="flex items-start gap-2 p-2 rounded-md bg-green-500/5 border border-green-500/20"
                      >
                        <TrendingUp className="h-4 w-4 text-green-600 shrink-0 mt-0.5" />
                        <span className="text-sm">{strength}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Skill Gaps */}
              {comparisonData.skillGaps && comparisonData.skillGaps.length > 0 && (
                <div className="space-y-3">
                  <h4 className="font-semibold flex items-center gap-2">
                    <AlertTriangle className="h-4 w-4 text-yellow-600" />
                    Skill Gaps ({comparisonData.skillGaps.length})
                  </h4>
                  <div className="grid gap-3">
                    {comparisonData.skillGaps.map((gap: SkillGap, index: number) => (
                      <div 
                        key={index}
                        className="p-3 rounded-md bg-yellow-500/5 border border-yellow-500/20"
                      >
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-medium text-sm">{gap.skillName}</span>
                          <Badge variant={getImportanceBadgeColor(gap.importance)}>
                            {gap.importance}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground">{gap.suggestion}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Recommendations */}
              {comparisonData.recommendations && comparisonData.recommendations.length > 0 && (
                <div className="space-y-3">
                  <h4 className="font-semibold flex items-center gap-2">
                    <Lightbulb className="h-4 w-4 text-blue-600" />
                    Recommendations ({comparisonData.recommendations.length})
                  </h4>
                  <div className="grid gap-2">
                    {comparisonData.recommendations.map((rec, index) => (
                      <div 
                        key={index}
                        className="flex items-start gap-2 p-2 rounded-md bg-blue-500/5 border border-blue-500/20"
                      >
                        <span className="text-blue-600 font-semibold text-sm shrink-0">{index + 1}.</span>
                        <span className="text-sm">{rec}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <Separator />

              {/* Job Description */}
              {jobData && jobData.rawContent && (
                <div className="space-y-3">
                  <h4 className="font-semibold">Job Description</h4>
                  <div className="rounded-lg border bg-muted/30 p-4">
                    <ScrollArea className="h-48">
                      <article className="prose prose-sm dark:prose-invert max-w-none">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                          {jobData.rawContent}
                        </ReactMarkdown>
                      </article>
                    </ScrollArea>
                  </div>
                </div>
              )}

              <div className="pt-4">
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full"
                  asChild
                >
                  <a 
                    href={`https://www.linkedin.com/jobs/view/${comparison.jobId}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-2"
                  >
                    <ExternalLink className="h-4 w-4" />
                    View on LinkedIn
                  </a>
                </Button>
              </div>
            </div>
          ) : (
            <div className="py-12 text-center space-y-3">
              <AlertTriangle className="h-8 w-8 text-muted-foreground mx-auto" />
              <p className="text-sm text-muted-foreground">Could not load comparison details</p>
            </div>
          )}
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
}

interface ScoreCardProps {
  label: string;
  score: number;
  icon?: React.ReactNode;
}

function ScoreCard({ label, score, icon }: ScoreCardProps) {
  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600 border-green-500/30 bg-green-500/5";
    if (score >= 60) return "text-yellow-600 border-yellow-500/30 bg-yellow-500/5";
    return "text-red-600 border-red-500/30 bg-red-500/5";
  };

  return (
    <div className={`p-4 rounded-lg border text-center ${getScoreColor(score)}`}>
      {icon && <div className="flex justify-center mb-1">{icon}</div>}
      <div className="text-2xl font-bold">{score}%</div>
      <div className="text-xs opacity-80">{label}</div>
    </div>
  );
}
