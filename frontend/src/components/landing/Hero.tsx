import { Button } from "@/components/ui/button";
import { ArrowRight, Play } from "lucide-react";
import { useState } from "react";
import AuthModal from "@/components/landing/AuthModal";

const Hero = () => {
   const [modalOpen, setModalOpen] = useState(false);
  return (
    <>
    <section className="hero-gradient relative overflow-hidden">
      {/* Decorative elements */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-20 left-10 w-72 h-72 rounded-full bg-accent blur-3xl" />
        <div className="absolute bottom-10 right-20 w-96 h-96 rounded-full bg-accent blur-3xl" />
      </div>

      <div className="relative max-w-7xl mx-auto px-6 py-24 lg:py-36">
       <div className="max-w-3xl">
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-primary-foreground leading-tight tracking-tight mb-6">
            Smarter exams,{" "}
            <span className="text-gradient">seamless results.</span>
          </h1>

          <p className="text-lg sm:text-xl text-primary-foreground/70 max-w-2xl mb-10 leading-relaxed">
            Create, manage, and deliver secure online examinations with real-time proctoring, 
            automated grading, and powerful analytics — all in one platform.
          </p>

          <div className="flex flex-col sm:flex-row gap-4">
            <Button variant="hero" size="lg">
              Start free trial
              <ArrowRight className="ml-1 h-5 w-5" />
            </Button>
            <Button variant="heroOutline" size="lg">
              <Play className="mr-1 h-4 w-4" />
              Watch demo
            </Button>
          </div>

        </div>
      </div>
    </section>
     {/* Auth Modal */}
      <AuthModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </>
  );
};

export default Hero;
