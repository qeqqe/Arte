"use client";

import { useState, useRef } from "react";
import { Loader2, CheckCircle2, Upload, FileText, X } from "lucide-react";
import { useIngestResume, useProfile } from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface ResumeStepProps {
  isComplete: boolean;
  onComplete: () => void;
}

export function ResumeStep({ isComplete, onComplete }: ResumeStepProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [resumeText, setResumeText] = useState("");
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { mutate: ingestResume, isPending, error } = useIngestResume();
  const { data: profile } = useProfile();
  const [hasIngested, setHasIngested] = useState(isComplete);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    const file = e.dataTransfer.files?.[0];
    if (file && (file.type === "application/pdf" || file.name.endsWith(".pdf") || file.name.endsWith(".txt"))) {
      setSelectedFile(file);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
    }
  };

  const handleFileUpload = () => {
    if (!selectedFile) return;

    ingestResume(selectedFile, {
      onSuccess: (data) => {
        if (data.success) {
          setHasIngested(true);
        }
      },
    });
  };

  const handleTextSubmit = () => {
    if (!resumeText.trim()) return;

    const blob = new Blob([resumeText], { type: "text/plain" });
    const file = new File([blob], "resume.txt", { type: "text/plain" });

    ingestResume(file, {
      onSuccess: (data) => {
        if (data.success) {
          setHasIngested(true);
        }
      },
    });
  };

  if (hasIngested && profile?.resumeSummary) {
    const summary = profile.resumeSummary;
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-green-600">
          <CheckCircle2 className="h-5 w-5" />
          <span className="font-medium">Resume processed successfully</span>
        </div>

        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
            <FileText className="h-5 w-5 text-muted-foreground" />
            <div>
              <div className="text-sm font-medium">{summary.fileName}</div>
              <div className="text-xs text-muted-foreground">{summary.wordCount} words</div>
            </div>
          </div>

          {summary.skills && summary.skills.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Extracted Skills</div>
              <div className="flex flex-wrap gap-2">
                {summary.skills.slice(0, 12).map((skill, i) => (
                  <Badge key={i} variant="secondary">
                    {skill}
                  </Badge>
                ))}
                {summary.skills.length > 12 && (
                  <Badge variant="outline">+{summary.skills.length - 12} more</Badge>
                )}
              </div>
            </div>
          )}

          {summary.education && summary.education.length > 0 && (
            <div>
              <div className="text-sm font-medium mb-2">Education</div>
              <div className="space-y-1">
                {summary.education.slice(0, 3).map((edu, i) => (
                  <div key={i} className="text-sm text-muted-foreground">{edu}</div>
                ))}
              </div>
            </div>
          )}
        </div>

        <Button onClick={onComplete} className="w-full">
          Complete Setup
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <p className="text-sm text-muted-foreground">
        Upload your resume or paste the content below. We&apos;ll extract your skills, 
        experience, and education to match you with relevant opportunities.
      </p>

      <Tabs defaultValue="upload" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="upload">Upload File</TabsTrigger>
          <TabsTrigger value="paste">Paste Text</TabsTrigger>
        </TabsList>

        <TabsContent value="upload" className="space-y-4">
          <div
            className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
              dragActive 
                ? "border-primary bg-primary/5" 
                : "border-muted-foreground/25 hover:border-muted-foreground/50"
            }`}
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.txt"
              onChange={handleFileChange}
              className="hidden"
            />
            
            {selectedFile ? (
              <div className="flex items-center justify-center gap-3">
                <FileText className="h-8 w-8 text-muted-foreground" />
                <div className="text-left">
                  <div className="font-medium">{selectedFile.name}</div>
                  <div className="text-sm text-muted-foreground">
                    {(selectedFile.size / 1024).toFixed(1)} KB
                  </div>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => setSelectedFile(null)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <div className="space-y-2">
                <Upload className="h-8 w-8 mx-auto text-muted-foreground" />
                <div>
                  <Button
                    variant="link"
                    className="p-0 h-auto"
                    onClick={() => fileInputRef.current?.click()}
                  >
                    Click to upload
                  </Button>
                  <span className="text-muted-foreground"> or drag and drop</span>
                </div>
                <div className="text-xs text-muted-foreground">PDF or TXT (max 5MB)</div>
              </div>
            )}
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-destructive/10 text-destructive text-sm">
              Failed to process resume. Please try again.
            </div>
          )}

          <Button
            onClick={handleFileUpload}
            disabled={!selectedFile || isPending}
            className="w-full gap-2"
          >
            {isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : (
              "Upload Resume"
            )}
          </Button>
        </TabsContent>

        <TabsContent value="paste" className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="resume-text">Resume Content</Label>
            <Textarea
              id="resume-text"
              placeholder="Paste your resume text here..."
              value={resumeText}
              onChange={(e) => setResumeText(e.target.value)}
              className="min-h-[200px] resize-none"
              disabled={isPending}
            />
            <div className="text-xs text-muted-foreground text-right">
              {resumeText.split(/\s+/).filter(Boolean).length} words
            </div>
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-destructive/10 text-destructive text-sm">
              Failed to process resume. Please try again.
            </div>
          )}

          <Button
            onClick={handleTextSubmit}
            disabled={!resumeText.trim() || isPending}
            className="w-full gap-2"
          >
            {isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : (
              "Submit Resume"
            )}
          </Button>
        </TabsContent>
      </Tabs>
    </div>
  );
}
