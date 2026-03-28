const steps = [
  {
    number: "01",
    title: "Create Your Exam",
    description: "Build exams using our intuitive editor. Add MCQs, essays, coding challenges, and more from your question bank.",
  },
  {
    number: "02",
    title: "Invite Candidates",
    description: "Send secure exam links to candidates via email or integrate with your LMS. Set schedules and time limits.",
  },
  {
    number: "03",
    title: "Monitor & Grade",
    description: "Watch live proctoring feeds, let AI auto-grade responses, and review flagged submissions.",
  },
  {
    number: "04",
    title: "Analyze & Certify",
    description: "Generate detailed reports, export results, and issue digital certificates — all automatically.",
  },
];

const HowItWorks = () => {
  return (
    <section className="py-24 bg-muted/50" id="how-it-works">
      <div className="max-w-7xl mx-auto px-6">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <p className="text-sm font-semibold uppercase tracking-widest text-accent mb-3">How It Works</p>
          <h2 className="text-3xl sm:text-4xl font-bold text-foreground mb-4">
            Four simple steps to go live
          </h2>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
          {steps.map((step, index) => (
            <div key={step.number} className="relative">
              {index < steps.length - 1 && (
                <div className="hidden lg:block absolute top-8 left-full w-full h-px bg-border -translate-x-4" />
              )}
              <div className="text-5xl font-bold text-accent/20 mb-4 font-display">{step.number}</div>
              <h3 className="text-lg font-semibold text-foreground mb-2">{step.title}</h3>
              <p className="text-muted-foreground leading-relaxed">{step.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default HowItWorks;
