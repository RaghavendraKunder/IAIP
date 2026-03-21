import { useState, useEffect, useRef } from "react";
import {
  X, GraduationCap, Eye, EyeOff, ArrowRight,
  ShieldCheck, Users, ChevronDown,
} from "lucide-react";
import { cn } from "@/lib/utils";

// ─── Types ────────────────────────────────────────────────────────────────────
type Tab  = "admin" | "student";
type Mode = "login" | "signup";

type ClassLevel =
  | "" | "1" | "2" | "3" | "4" | "5"
  | "6" | "7" | "8" | "9" | "10"
  | "11" | "12" | "UG" | "PG";

type Stream = "" | "Science" | "Arts" | "Commerce";

interface AuthModalProps {
  open: boolean;
  onClose: () => void;
}

// ─── Shared base input class ──────────────────────────────────────────────────
const inputCls = cn(
  "w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm text-foreground",
  "placeholder:text-muted-foreground/60 outline-none transition-all duration-200",
  "focus:border-[hsl(243,75%,58%)] focus:ring-2 focus:ring-[hsl(243,75%,58%)]/20",
);

// ─── Text / password input ────────────────────────────────────────────────────
function Field({
  label, type = "text", placeholder, value, onChange,
}: {
  label: string; type?: string;
  placeholder?: string; value: string;
  onChange: (v: string) => void;
}) {
  const [show, setShow] = useState(false);
  const isPassword = type === "password";
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </label>
      <div className="relative">
        <input
          type={isPassword && show ? "text" : type}
          placeholder={placeholder}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={cn(inputCls, isPassword && "pr-11")}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setShow(!show)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
          >
            {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        )}
      </div>
    </div>
  );
}

// ─── Select dropdown ──────────────────────────────────────────────────────────
function SelectField({
  label, value, onChange, options, placeholder,
}: {
  label: string; value: string;
  onChange: (v: string) => void;
  options: { value: string; label: string }[];
  placeholder?: string;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </label>
      <div className="relative">
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={cn(inputCls, "appearance-none pr-10 cursor-pointer")}
        >
          {placeholder && <option value="" disabled>{placeholder}</option>}
          {options.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
        <ChevronDown className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      </div>
    </div>
  );
}

// ─── Data ─────────────────────────────────────────────────────────────────────
const CLASS_OPTIONS: { value: ClassLevel; label: string }[] = [
  { value: "1",  label: "Class 1"  }, { value: "2",  label: "Class 2"  },
  { value: "3",  label: "Class 3"  }, { value: "4",  label: "Class 4"  },
  { value: "5",  label: "Class 5"  }, { value: "6",  label: "Class 6"  },
  { value: "7",  label: "Class 7"  }, { value: "8",  label: "Class 8"  },
  { value: "9",  label: "Class 9"  }, { value: "10", label: "Class 10" },
  { value: "11", label: "Class 11" }, { value: "12", label: "Class 12" },
  { value: "UG", label: "Under Graduate (UG)" },
  { value: "PG", label: "Post Graduate (PG)"  },
];

const STREAM_OPTIONS = [
  { value: "Science",  emoji: "🔬", label: "Science"  },
  { value: "Arts",     emoji: "🎨", label: "Arts"     },
  { value: "Commerce", emoji: "💼", label: "Commerce" },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────
const needsStream = (c: ClassLevel) => ["11", "12", "UG", "PG"].includes(c);
const needsCourse = (c: ClassLevel) => c === "UG";

// ─── Backend base URL ─────────────────────────────────────────────────────────
const BASE = "http://localhost:8085/Online_Examination_System";

// ─── Component ────────────────────────────────────────────────────────────────
export default function AuthModal({ open, onClose }: AuthModalProps) {

  // ── All useState hooks declared at the top — before any useEffect ─────────
  const [tab,  setTab]  = useState<Tab>("student");
  const [mode, setMode] = useState<Mode>("login");
  const overlayRef      = useRef<HTMLDivElement>(null);

  // submit feedback state
  const [submitState, setSubmitState] = useState<{
    loading: boolean;
    error:   string;
    success: string;
  }>({ loading: false, error: "", success: "" });

  // common fields
  const [name,     setName]     = useState("");
  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [confirm,  setConfirm]  = useState("");

  // admin-only fields
  const [adminKey,     setAdminKey]     = useState("");
  const [adminCollege, setAdminCollege] = useState("");

  // student signup fields
  const [rollNo,     setRollNo]     = useState("");
  const [college,    setCollege]    = useState("");
  const [classLevel, setClassLevel] = useState<ClassLevel>("");
  const [stream,     setStream]     = useState<Stream>("");
  const [course,     setCourse]     = useState("");

  // ── Reset all fields + submitState when tab or mode changes ──────────────
  useEffect(() => {
    setName(""); setEmail(""); setPassword(""); setConfirm("");
    setAdminKey(""); setAdminCollege("");
    setRollNo(""); setCollege(""); setClassLevel(""); setStream(""); setCourse("");
    setSubmitState({ loading: false, error: "", success: "" });
  }, [tab, mode]);

  // ── Reset stream + course when class changes ──────────────────────────────
  useEffect(() => {
    setStream("");
    setCourse("");
  }, [classLevel]);

  // ── Escape key closes modal ───────────────────────────────────────────────
  useEffect(() => {
    const h = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    window.addEventListener("keydown", h);
    return () => window.removeEventListener("keydown", h);
  }, [onClose]);

  // ── Body scroll lock when modal is open ──────────────────────────────────
  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  // ── Early return after all hooks ──────────────────────────────────────────
  if (!open) return null;

  // ── Derived booleans ──────────────────────────────────────────────────────
  const isSignup        = mode === "signup";
  const isStudentSignup = tab === "student" && isSignup;
  const isAdminSignup   = tab === "admin"   && isSignup;
  const showStream      = isStudentSignup && needsStream(classLevel);
  const showCourse      = isStudentSignup && needsCourse(classLevel);

  // ── Submit handler ────────────────────────────────────────────────────────
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitState({ loading: true, error: "", success: "" });

    // ── SIGNUP ───────────────────────────────────────────────────────────────
    if (isSignup) {
      const payload =
        tab === "student"
          ? {
              role:            "student",
              fullName:        name,
              rollNo,
              email,
              password,
              confirmPassword: confirm,
              collegeName:     college,
              classLevel,
              stream,
              course,
            }
          : {
              role:            "admin",
              fullName:        name,
              email,
              password,
              confirmPassword: confirm,
              collegeName:     adminCollege,
              adminAccessKey:  adminKey,
            };

      try {
        const res  = await fetch(`${BASE}/api/register`, {
          method:  "POST",
          headers: { "Content-Type": "application/json" },
          body:    JSON.stringify(payload),
        });
        const data = await res.json();

        if (data.success) {
          setSubmitState({ loading: false, error: "", success: data.message });
        } else {
          setSubmitState({ loading: false, error: data.message, success: "" });
        }
      } catch {
        setSubmitState({
          loading: false,
          error:   "Could not reach the server. Make sure Tomcat is running.",
          success: "",
        });
      }
      return;
    }

    // ── LOGIN ────────────────────────────────────────────────────────────────
    const payload =
      tab === "student"
        ? {
            role:       "student",
            identifier: email,   // accepts roll number OR email
            password,
          }
        : {
            role:       "admin",
            identifier: email,
            password,
            adminKey,
          };

    try {
      const res  = await fetch(`${BASE}/api/login`, {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify(payload),
      });
      const data = await res.json();

      if (data.success) {
        setSubmitState({ loading: false, error: "", success: data.message });
        // Store session info for static HTML pages
        if (data.role === "admin") {
          sessionStorage.setItem("adminName",   data.adminName   || "Admin");
          sessionStorage.setItem("adminId",     String(data.adminId || ""));
          sessionStorage.setItem("collegeName", data.collegeName || "");
        } else if (data.role === "student") {
          sessionStorage.setItem("studentId",   String(data.studentId   || ""));
          sessionStorage.setItem("studentName", data.studentName || "Student");
          sessionStorage.setItem("rollNo",      data.rollNo      || "");
        }
        setTimeout(() => {
          const TOMCAT = "http://localhost:8085/Online_Examination_System";
          if (data.role === "admin") {
            window.location.href = window.location.origin + "/admin-dashboard.html";
          } else {
            // Store student data FIRST, then redirect
            sessionStorage.setItem("studentId",   String(data.studentId   || ""));
            sessionStorage.setItem("studentName", data.studentName || "Student");
            sessionStorage.setItem("rollNo",      data.rollNo      || "");
            window.location.href = window.location.origin + "/student-dashboard.html";
          }
        }, 1000);
      } else {
        setSubmitState({ loading: false, error: data.message, success: "" });
      }
    } catch {
      setSubmitState({
        loading: false,
        error:   "Could not reach the server. Make sure Tomcat is running.",
        success: "",
      });
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <div
      ref={overlayRef}
      onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ background: "hsl(220 65% 8% / 0.72)", backdropFilter: "blur(6px)" }}
    >
      {/* ── Modal shell ─────────────────────────────────────────────────────── */}
      <div
        className={cn(
          "relative w-full max-w-md rounded-2xl border border-border bg-card shadow-2xl",
          "animate-in fade-in-0 zoom-in-95 duration-200",
          "max-h-[92vh] overflow-y-auto",
        )}
        style={{ boxShadow: "0 24px 64px -12px hsl(220 65% 8% / 0.35)" }}
      >
        {/* gradient top bar */}
        <div
          className="sticky top-0 h-1 rounded-t-2xl z-10"
          style={{ background: "linear-gradient(90deg, hsl(243,75%,58%), hsl(258,89%,66%))" }}
        />

        {/* ── Header ────────────────────────────────────────────────────────── */}
        <div className="px-7 pt-7 pb-4">
          <div className="flex items-center justify-between mb-5">
            <div className="flex items-center gap-2">
              <GraduationCap className="h-6 w-6" style={{ color: "hsl(258,89%,66%)" }} />
              <span className="font-display font-bold text-xl text-foreground tracking-tight">
                ExamFlow
              </span>
            </div>
            <button
              onClick={onClose}
              className="rounded-lg p-1.5 text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
          <h2 className="font-display font-bold text-2xl text-foreground tracking-tight mb-1">
            {isSignup ? "Create your account" : "Welcome back"}
          </h2>
          <p className="text-sm text-muted-foreground">
            {isSignup
              ? "Join ExamFlow and start your journey"
              : "Sign in to continue to your dashboard"}
          </p>
        </div>

        {/* ── Role tabs ─────────────────────────────────────────────────────── */}
        <div className="px-7 mb-5">
          <div className="flex rounded-xl border border-border bg-muted/40 p-1 gap-1">
            {(["student", "admin"] as Tab[]).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setTab(t)}
                className={cn(
                  "flex-1 flex items-center justify-center gap-2 rounded-lg py-2.5 text-sm font-semibold transition-all duration-200",
                  tab === t
                    ? "bg-card text-foreground shadow-sm border border-border"
                    : "text-muted-foreground hover:text-foreground",
                )}
              >
                {t === "student"
                  ? <Users className="h-4 w-4" />
                  : <ShieldCheck className="h-4 w-4" />}
                {t.charAt(0).toUpperCase() + t.slice(1)}
              </button>
            ))}
          </div>
          <p className="mt-2 text-xs text-center text-muted-foreground">
            {tab === "student"
              ? "Access and attempt exams assigned to you"
              : "Manage exams, students, and results"}
          </p>
        </div>

        {/* ── Form ──────────────────────────────────────────────────────────── */}
        <form onSubmit={handleSubmit} className="px-7 pb-4 flex flex-col gap-4">

          {/* Full name — any signup */}
          {isSignup && (
            <Field
              label="Full Name"
              placeholder="e.g. Arjun Sharma"
              value={name}
              onChange={setName}
            />
          )}

          {/* Roll number — student signup only */}
          {isStudentSignup && (
            <Field
              label="Roll Number"
              placeholder="e.g. 2024CS001"
              value={rollNo}
              onChange={setRollNo}
            />
          )}

          {/* Email — or Roll+Email for student login */}
          <Field
            label={tab === "student" && !isSignup ? "Roll Number / Email" : "Email Address"}
            placeholder={
              tab === "student" && !isSignup
                ? "e.g. 2024CS001 or you@college.edu"
                : "you@institution.edu"
            }
            value={email}
            onChange={setEmail}
          />

          {/* College name — student signup */}
          {isStudentSignup && (
            <Field
              label="College / School Name"
              placeholder="e.g. St. Xavier's College, Mumbai"
              value={college}
              onChange={setCollege}
            />
          )}

          {/* ── Admin signup: institution + security banner + access key ────── */}
          {isAdminSignup && (
            <>
              <Field
                label="College / Institution Name"
                placeholder="e.g. St. Xavier's College, Mumbai"
                value={adminCollege}
                onChange={setAdminCollege}
              />

              {/* Security banner */}
              <div
                className="flex items-start gap-2.5 rounded-xl px-3.5 py-3 text-xs"
                style={{
                  background: "hsl(38,92%,50%,0.08)",
                  color:      "hsl(32,95%,34%)",
                  border:     "1px solid hsl(38,92%,50%,0.25)",
                }}
              >
                <ShieldCheck className="h-4 w-4 flex-shrink-0 mt-0.5" />
                <span>
                  The <strong>Admin Access Key</strong> is issued by your institution.
                  Contact your IT administrator if you don't have one.
                </span>
              </div>

              <Field
                label="Admin Access Key"
                type="password"
                placeholder="Enter the key issued by your institution"
                value={adminKey}
                onChange={setAdminKey}
              />
            </>
          )}

          {/* Class / Year — student signup */}
          {isStudentSignup && (
            <SelectField
              label="Class / Year"
              value={classLevel}
              onChange={(v) => setClassLevel(v as ClassLevel)}
              options={CLASS_OPTIONS}
              placeholder="Select your class or year"
            />
          )}

          {/* ── Stream card picker (Class 11, 12, UG, PG) ─────────────────── */}
          {showStream && (
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Stream
              </label>

              <div
                className="flex items-center gap-2 rounded-xl px-3 py-2 text-xs"
                style={{
                  background: "hsl(250,100%,97%)",
                  color:      "hsl(258,89%,66%)",
                  border:     "1px solid hsl(258,89%,66%,0.18)",
                }}
              >
                <GraduationCap className="h-3.5 w-3.5 flex-shrink-0" />
                {classLevel === "UG" || classLevel === "PG"
                  ? "Select the stream your degree falls under"
                  : `Class ${classLevel} detected — select your stream`}
              </div>

              <div className="grid grid-cols-3 gap-2">
                {STREAM_OPTIONS.map((s) => {
                  const active = stream === s.value;
                  return (
                    <button
                      key={s.value}
                      type="button"
                      onClick={() => setStream(s.value as Stream)}
                      className={cn(
                        "flex flex-col items-center justify-center gap-1.5 rounded-xl border py-3.5 px-2",
                        "text-xs font-semibold transition-all duration-200",
                        active
                          ? "border-[hsl(243,75%,58%)] bg-[hsl(243,75%,58%)]/10 text-[hsl(243,75%,58%)] shadow-sm"
                          : "border-border bg-background text-muted-foreground hover:border-[hsl(243,75%,58%)]/40 hover:text-foreground",
                      )}
                    >
                      <span className="text-xl">{s.emoji}</span>
                      <span>{s.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* ── Course field (UG only) ───────────────────────────────────────── */}
          {showCourse && (
            <div className="flex flex-col gap-1.5">
              <div
                className="flex items-center gap-2 rounded-xl px-3 py-2 text-xs"
                style={{
                  background: "hsl(142,76%,36%,0.08)",
                  color:      "hsl(142,76%,28%)",
                  border:     "1px solid hsl(142,76%,36%,0.2)",
                }}
              >
                <GraduationCap className="h-3.5 w-3.5 flex-shrink-0" />
                Under Graduate selected — enter the degree you are pursuing
              </div>
              <Field
                label="Course / Degree Programme"
                placeholder="e.g. B.Sc Computer Science · B.Com · B.A. Psychology"
                value={course}
                onChange={setCourse}
              />
            </div>
          )}

          {/* Password */}
          <Field
            label="Password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={setPassword}
          />

          {/* Confirm password — signup only */}
          {isSignup && (
            <Field
              label="Confirm Password"
              type="password"
              placeholder="Re-enter your password"
              value={confirm}
              onChange={setConfirm}
            />
          )}

          {/* Admin access key — admin LOGIN only */}
          {tab === "admin" && !isSignup && (
            <Field
              label="Admin Access Key"
              type="password"
              placeholder="Issued by your institution"
              value={adminKey}
              onChange={setAdminKey}
            />
          )}

          {/* Forgot password */}
          {!isSignup && (
            <div className="flex justify-end -mt-1">
              <button
                type="button"
                className="text-xs font-medium transition-colors"
                style={{ color: "hsl(243,75%,58%)" }}
              >
                Forgot password?
              </button>
            </div>
          )}

          {/* ── Error banner ───────────────────────────────────────────────────── */}
          {submitState.error && (
            <div
              className="flex items-center gap-2 rounded-xl px-3.5 py-3 text-xs font-medium"
              style={{
                background: "hsl(0,72%,50%,0.08)",
                color:      "hsl(0,72%,40%)",
                border:     "1px solid hsl(0,72%,50%,0.2)",
              }}
            >
              <span className="text-base">⚠️</span>
              {submitState.error}
            </div>
          )}

          {/* ── Success banner ─────────────────────────────────────────────────── */}
          {submitState.success && (
            <div
              className="flex items-center gap-2 rounded-xl px-3.5 py-3 text-xs font-medium"
              style={{
                background: "hsl(142,76%,36%,0.08)",
                color:      "hsl(142,76%,28%)",
                border:     "1px solid hsl(142,76%,36%,0.2)",
              }}
            >
              <span className="text-base">✅</span>
              {submitState.success}
            </div>
          )}

          {/* ── Submit button ──────────────────────────────────────────────────── */}
          <button
            type="submit"
            disabled={submitState.loading}
            className={cn(
              "mt-1 w-full flex items-center justify-center gap-2",
              "rounded-xl py-3 px-6 text-sm font-semibold text-white",
              "transition-all duration-200 hover:-translate-y-0.5",
              "focus:outline-none focus:ring-2 focus:ring-offset-2",
              "disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:translate-y-0",
            )}
            style={{
              background: "linear-gradient(135deg, hsl(220,65%,18%), hsl(220,55%,28%), hsl(200,50%,30%))",
              boxShadow:  "0 4px 14px 0 hsl(243,75%,58%,0.35)",
            }}
            onMouseEnter={(e) => {
              if (!submitState.loading)
                (e.currentTarget as HTMLButtonElement).style.boxShadow =
                  "0 6px 20px 0 hsl(243,75%,58%,0.5)";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLButtonElement).style.boxShadow =
                "0 4px 14px 0 hsl(243,75%,58%,0.35)";
            }}
          >
            {submitState.loading ? (
              <>
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                  <circle
                    className="opacity-25" cx="12" cy="12" r="10"
                    stroke="currentColor" strokeWidth="4"
                  />
                  <path
                    className="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v8H4z"
                  />
                </svg>
                {isSignup ? "Creating account…" : "Signing in…"}
              </>
            ) : (
              <>
                {isSignup
                  ? "Create Account"
                  : `Sign in as ${tab.charAt(0).toUpperCase() + tab.slice(1)}`}
                <ArrowRight className="h-4 w-4" />
              </>
            )}
          </button>

          {/* Divider */}
          <div className="flex items-center gap-3 my-0.5">
            <div className="flex-1 h-px bg-border" />
            <span className="text-xs text-muted-foreground">
              {isSignup ? "Already have an account?" : "New to ExamFlow?"}
            </span>
            <div className="flex-1 h-px bg-border" />
          </div>

          {/* Mode toggle */}
          <button
            type="button"
            onClick={() => setMode(isSignup ? "login" : "signup")}
            className={cn(
              "w-full rounded-xl border border-border bg-background py-2.5 text-sm font-semibold",
              "text-foreground hover:border-[hsl(243,75%,58%)] hover:text-[hsl(243,75%,58%)]",
              "transition-all duration-200",
            )}
          >
            {isSignup ? "Sign in to existing account" : "Create a new account"}
          </button>
        </form>

        {/* ── Footer ────────────────────────────────────────────────────────── */}
        <div
          className="mx-7 mb-6 mt-2 rounded-xl px-4 py-3 text-xs text-center"
          style={{
            background: "hsl(250,100%,97%)",
            color:      "hsl(258,89%,66%)",
            border:     "1px solid hsl(258,89%,66%,0.2)",
          }}
        >
          🔒 Secured with end-to-end encryption · SOC 2 compliant
        </div>
      </div>
    </div>
  );
}