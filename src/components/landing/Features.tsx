import { Shield, BarChart3, Clock, Users, Brain, Lock } from "lucide-react";

const features = [
  {
    icon: Clock,
    title: "Instant Auto-Grading",
    description: "Save hours with automated scoring for MCQs, fill-in-the-blanks, and even short-answer questions.",
  },
  {
    icon: BarChart3,
    title: "Advanced Analytics",
    description: "Track performance trends, item difficulty, and student progress with detailed visual reports.",
  },
  {
    icon: Brain,
    title: "Smart Question Bank",
    description: "Build, tag, and randomize questions from a centralized bank to create unique exam papers every time.",
  },
  {
    icon: Lock,
    title: "Enterprise Security",
    description: "End-to-end encryption, role-based access, and SOC 2 compliance keep your data safe.",
  },
];

const Features = () => {
  return (
    <section className="py-24 bg-background" id="features">
      <div className="max-w-7xl mx-auto px-6">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <p className="text-sm font-semibold uppercase tracking-widest text-accent mb-3">Features</p>
          <h2 className="text-3xl sm:text-4xl font-bold text-foreground mb-4">
            Everything you need to run exams online
          </h2>
          <p className="text-muted-foreground text-lg">
            From creation to certification — a complete examination lifecycle at your fingertips.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature) => (
            <div
              key={feature.title}
              className="group rounded-xl border border-border bg-card p-7 card-elevated"
            >
              <div className="mb-4 inline-flex items-center justify-center w-12 h-12 rounded-lg bg-accent/10 text-accent group-hover:glow-accent transition-shadow duration-300">
                <feature.icon className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-semibold text-card-foreground mb-2">{feature.title}</h3>
              <p className="text-muted-foreground leading-relaxed">{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default Features;
