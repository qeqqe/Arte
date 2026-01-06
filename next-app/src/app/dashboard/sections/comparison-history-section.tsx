"use client";

import { useState } from "react";
import { useComparisons, useJob } from "@/hooks/use-api";
import { 
  History, 
  ChevronRight, 
  Briefcase, 
  Building2, 
  Calendar,
  Target,
  ExternalLink
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import type { JobComparisonSummary } from "@/types";

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
  const { data: jobData } = useJob(comparison.jobId);

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600";
    if (score >= 60) return "text-yellow-600";
    return "text-red-600";
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh]">
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

        <ScrollArea className="max-h-[60vh]">
          <div className="space-y-6 pr-4">
            <div className="text-center py-4">
              <div className="text-sm text-muted-foreground mb-1">Overall Match</div>
              <div className={`text-5xl font-bold ${getScoreColor(comparison.matchScore)}`}>
                {comparison.matchScore}%
              </div>
            </div>

            <Separator />

            {jobData && jobData.rawContent && (
              <div>
                <h4 className="font-medium mb-3">Job Details</h4>
                <div className="mt-3 p-3 rounded-lg bg-muted/30">
                  <div className="text-xs text-muted-foreground mb-2">Description Preview</div>
                  <p className="text-sm line-clamp-6">{jobData.rawContent}</p>
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
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
}

interface ScoreCardProps {
  label: string;
  score: number;
}

function ScoreCard({ label, score }: ScoreCardProps) {
  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600 border-green-500/30 bg-green-500/5";
    if (score >= 60) return "text-yellow-600 border-yellow-500/30 bg-yellow-500/5";
    return "text-red-600 border-red-500/30 bg-red-500/5";
  };

  return (
    <div className={`p-4 rounded-lg border text-center ${getScoreColor(score)}`}>
      <div className="text-2xl font-bold">{score}%</div>
      <div className="text-xs opacity-80">{label}</div>
    </div>
  );
}
