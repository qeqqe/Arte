"use client";

import { useState } from "react";
import { 
  Search, 
  Loader2, 
  CheckCircle2, 
  ArrowRight,
  Briefcase,
  Building2,
  Clock,
  AlertCircle,
  Sparkles,
  Target
} from "lucide-react";
import { 
  useIngestLinkedInJob, 
  useJob, 
  useProcessJob, 
  useCompareUserJob,
  useProfile
} from "@/hooks/use-api";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Progress } from "@/components/ui/progress";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { ProcessedJobData, UserJobComparison } from "@/types";

type AnalysisStep = "search" | "fetched" | "processing" | "processed" | "comparing" | "complete";

export function JobAnalysisSection() {
  const [jobId, setJobId] = useState("");
  const [currentStep, setCurrentStep] = useState<AnalysisStep>("search");
  const [currentJobId, setCurrentJobId] = useState<string | null>(null);
  const [processedJob, setProcessedJob] = useState<ProcessedJobData | null>(null);
  const [comparison, setComparison] = useState<UserJobComparison | null>(null);

  const { data: profile } = useProfile();
  const { data: jobData, isLoading: isFetchingJob } = useJob(currentJobId || "");
  const { mutate: ingestJob, isPending: isIngesting, error: ingestError } = useIngestLinkedInJob();
  const { mutate: processJob, isPending: isProcessing } = useProcessJob();
  const { mutate: compareJob, isPending: isComparing } = useCompareUserJob();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!jobId.trim()) return;

    const cleanJobId = jobId.trim();
    setCurrentJobId(null);
    setProcessedJob(null);
    setComparison(null);

    ingestJob(cleanJobId, {
      onSuccess: (data) => {
        if (data.success) {
          setCurrentJobId(cleanJobId);
          setCurrentStep("fetched");
        }
      },
    });
  };

  const handleProcessJob = () => {
    if (!currentJobId) return;

    setCurrentStep("processing");
    processJob(currentJobId, {
      onSuccess: (data) => {
        if (data.success && data.processedData) {
          setProcessedJob(data.processedData);
          setCurrentStep("processed");
        }
      },
      onError: () => {
        setCurrentStep("fetched");
      },
    });
  };

  const handleCompare = () => {
    if (!currentJobId) return;

    setCurrentStep("comparing");
    compareJob(currentJobId, {
      onSuccess: (data) => {
        if (data.success && data.comparison) {
          setComparison(data.comparison);
          setCurrentStep("complete");
        }
      },
      onError: () => {
        setCurrentStep("processed");
      },
    });
  };

  const handleReset = () => {
    setJobId("");
    setCurrentJobId(null);
    setCurrentStep("search");
    setProcessedJob(null);
    setComparison(null);
  };

  const canCompare = profile?.hasProcessedData;

  return (
    <section className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold">Job Analysis</h2>
        <p className="text-sm text-muted-foreground">
          Analyze LinkedIn job postings and compare them against your profile
        </p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-lg flex items-center gap-2">
                <Search className="h-5 w-5" />
                Analyze a Job Posting
              </CardTitle>
              <CardDescription>
                Enter a LinkedIn job ID to start the analysis
              </CardDescription>
            </div>
            {currentStep !== "search" && (
              <Button variant="outline" size="sm" onClick={handleReset}>
                Start Over
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {currentStep === "search" && (
            <SearchStep
              jobId={jobId}
              onJobIdChange={setJobId}
              onSearch={handleSearch}
              isLoading={isIngesting}
              error={ingestError?.message}
            />
          )}

          {currentStep === "fetched" && jobData && (
            <FetchedStep
              jobData={jobData}
              onProcess={handleProcessJob}
              isLoading={isProcessing}
            />
          )}

          {currentStep === "processing" && (
            <ProcessingStep message="Analyzing job requirements..." />
          )}

          {currentStep === "processed" && processedJob && (
            <ProcessedStep
              job={processedJob}
              onCompare={handleCompare}
              canCompare={!!canCompare}
              isLoading={isComparing}
            />
          )}

          {currentStep === "comparing" && (
            <ProcessingStep message="Comparing your profile against job requirements..." />
          )}

          {currentStep === "complete" && comparison && processedJob && (
            <ComparisonResult
              comparison={comparison}
              job={processedJob}
            />
          )}
        </CardContent>
      </Card>
    </section>
  );
}

interface SearchStepProps {
  jobId: string;
  onJobIdChange: (value: string) => void;
  onSearch: (e: React.FormEvent) => void;
  isLoading: boolean;
  error?: string;
}

function SearchStep({ jobId, onJobIdChange, onSearch, isLoading, error }: SearchStepProps) {
  return (
    <form onSubmit={onSearch} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="job-id">LinkedIn Job ID</Label>
        <div className="flex gap-2">
          <Input
            id="job-id"
            placeholder="e.g., 3875421490"
            value={jobId}
            onChange={(e) => onJobIdChange(e.target.value)}
            disabled={isLoading}
            className="flex-1"
          />
          <Button type="submit" disabled={!jobId.trim() || isLoading}>
            {isLoading ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Search className="h-4 w-4" />
            )}
          </Button>
        </div>
        <p className="text-xs text-muted-foreground">
          Find the job ID in the LinkedIn job URL after &quot;/view/&quot;
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-2 p-3 rounded-lg bg-destructive/10 text-destructive text-sm">
          <AlertCircle className="h-4 w-4" />
          {error}
        </div>
      )}
    </form>
  );
}

interface FetchedStepProps {
  jobData: { rawContent: string; isProcessed: boolean };
  onProcess: () => void;
  isLoading: boolean;
}

function FetchedStep({ jobData, onProcess, isLoading }: FetchedStepProps) {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2 text-green-600">
        <CheckCircle2 className="h-5 w-5" />
        <span className="font-medium">Job posting fetched successfully</span>
      </div>

      <div className="rounded-lg border bg-muted/30 p-4">
        <ScrollArea className="h-48">
          <pre className="text-sm whitespace-pre-wrap font-mono">
            {jobData.rawContent.slice(0, 2000)}
            {jobData.rawContent.length > 2000 && "..."}
          </pre>
        </ScrollArea>
      </div>

      <Button onClick={onProcess} disabled={isLoading} className="w-full gap-2">
        {isLoading ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            Processing...
          </>
        ) : (
          <>
            <Sparkles className="h-4 w-4" />
            Analyze Job Requirements
          </>
        )}
      </Button>
    </div>
  );
}

function ProcessingStep({ message }: { message: string }) {
  return (
    <div className="py-12 text-center space-y-4">
      <div className="relative mx-auto w-16 h-16">
        <div className="absolute inset-0 rounded-full border-4 border-muted" />
        <div className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin" />
      </div>
      <div>
        <p className="font-medium">{message}</p>
        <p className="text-sm text-muted-foreground">This may take a moment</p>
      </div>
    </div>
  );
}

interface ProcessedStepProps {
  job: ProcessedJobData;
  onCompare: () => void;
  canCompare: boolean;
  isLoading: boolean;
}

function ProcessedStep({ job, onCompare, canCompare, isLoading }: ProcessedStepProps) {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 text-green-600">
        <CheckCircle2 className="h-5 w-5" />
        <span className="font-medium">Job analyzed successfully</span>
      </div>

      <div className="space-y-4">
        <div className="flex items-start gap-3">
          <Briefcase className="h-5 w-5 mt-0.5 text-muted-foreground" />
          <div>
            <div className="font-semibold text-lg">{job.jobTitle}</div>
            <div className="flex items-center gap-2 text-muted-foreground">
              <Building2 className="h-4 w-4" />
              <span>{job.company}</span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-4 text-sm">
          <Badge variant="outline">{job.careerLevel}</Badge>
          {(job.minYearsExperience > 0 || job.maxYearsExperience > 0) && (
            <span className="flex items-center gap-1 text-muted-foreground">
              <Clock className="h-4 w-4" />
              {job.minYearsExperience}-{job.maxYearsExperience} years
            </span>
          )}
        </div>

        <Separator />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {job.requiredSkills.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Required Skills</div>
              <div className="flex flex-wrap gap-1.5">
                {job.requiredSkills.slice(0, 8).map((skill, i) => (
                  <Badge key={i} variant="default" className="text-xs">
                    {skill}
                  </Badge>
                ))}
                {job.requiredSkills.length > 8 && (
                  <Badge variant="outline" className="text-xs">
                    +{job.requiredSkills.length - 8}
                  </Badge>
                )}
              </div>
            </div>
          )}

          {job.preferredSkills.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Preferred Skills</div>
              <div className="flex flex-wrap gap-1.5">
                {job.preferredSkills.slice(0, 6).map((skill, i) => (
                  <Badge key={i} variant="secondary" className="text-xs">
                    {skill}
                  </Badge>
                ))}
                {job.preferredSkills.length > 6 && (
                  <Badge variant="outline" className="text-xs">
                    +{job.preferredSkills.length - 6}
                  </Badge>
                )}
              </div>
            </div>
          )}
        </div>

        {job.programmingLanguages.length > 0 && (
          <div>
            <div className="text-sm font-medium mb-2">Tech Stack</div>
            <div className="flex flex-wrap gap-1.5">
              {[...job.programmingLanguages, ...job.frameworks, ...job.tools]
                .slice(0, 12)
                .map((tech, i) => (
                  <Badge key={i} variant="outline" className="text-xs">
                    {tech}
                  </Badge>
                ))}
            </div>
          </div>
        )}
      </div>

      {!canCompare && (
        <div className="flex items-center gap-2 p-3 rounded-lg bg-yellow-500/10 text-yellow-700 dark:text-yellow-400 text-sm">
          <AlertCircle className="h-4 w-4" />
          Process your profile first to enable job comparison
        </div>
      )}

      <Button 
        onClick={onCompare} 
        disabled={!canCompare || isLoading} 
        className="w-full gap-2"
      >
        {isLoading ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            Comparing...
          </>
        ) : (
          <>
            <Target className="h-4 w-4" />
            Compare Against My Profile
            <ArrowRight className="h-4 w-4" />
          </>
        )}
      </Button>
    </div>
  );
}

interface ComparisonResultProps {
  comparison: UserJobComparison;
  job: ProcessedJobData;
}

function ComparisonResult({ comparison, job }: ComparisonResultProps) {
  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600";
    if (score >= 60) return "text-yellow-600";
    return "text-red-600";
  };

  const getScoreBg = (score: number) => {
    if (score >= 80) return "bg-green-500";
    if (score >= 60) return "bg-yellow-500";
    return "bg-red-500";
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-2">
        <div className="text-sm text-muted-foreground">Overall Match</div>
        <div className={`text-5xl font-bold ${getScoreColor(comparison.overallMatchScore)}`}>
          {comparison.overallMatchScore}%
        </div>
        <div className="text-lg">{job.jobTitle} at {job.company}</div>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <div className="text-center space-y-1">
          <Progress 
            value={comparison.skillsMatchScore} 
            className="h-2"
          />
          <div className="text-sm font-medium">{comparison.skillsMatchScore}%</div>
          <div className="text-xs text-muted-foreground">Skills</div>
        </div>
        <div className="text-center space-y-1">
          <Progress 
            value={comparison.experienceMatchScore} 
            className="h-2"
          />
          <div className="text-sm font-medium">{comparison.experienceMatchScore}%</div>
          <div className="text-xs text-muted-foreground">Experience</div>
        </div>
        <div className="text-center space-y-1">
          <Progress 
            value={comparison.educationMatchScore} 
            className="h-2"
          />
          <div className="text-sm font-medium">{comparison.educationMatchScore}%</div>
          <div className="text-xs text-muted-foreground">Education</div>
        </div>
      </div>

      <Separator />

      <div className="p-4 rounded-lg bg-muted/30">
        <div className="text-sm font-medium mb-2">Assessment</div>
        <p className="text-sm text-muted-foreground">{comparison.fitAssessment}</p>
      </div>

      {comparison.strengths.length > 0 && (
        <div>
          <div className="text-sm font-medium mb-2 text-green-600">Your Strengths</div>
          <ul className="space-y-1">
            {comparison.strengths.map((strength, i) => (
              <li key={i} className="text-sm flex items-start gap-2">
                <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 shrink-0" />
                {strength}
              </li>
            ))}
          </ul>
        </div>
      )}

      {comparison.skillGaps.length > 0 && (
        <div>
          <div className="text-sm font-medium mb-2 text-yellow-600">Skill Gaps</div>
          <div className="space-y-2">
            {comparison.skillGaps.map((gap, i) => (
              <div key={i} className="p-3 rounded-lg bg-muted/30">
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium text-sm">{gap.skillName}</span>
                  <Badge variant="outline" className="text-xs">{gap.importance}</Badge>
                </div>
                <p className="text-xs text-muted-foreground">{gap.suggestion}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {comparison.recommendations.length > 0 && (
        <div>
          <div className="text-sm font-medium mb-2">Recommendations</div>
          <ul className="space-y-1">
            {comparison.recommendations.map((rec, i) => (
              <li key={i} className="text-sm text-muted-foreground flex items-start gap-2">
                <ArrowRight className="h-4 w-4 mt-0.5 shrink-0" />
                {rec}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
