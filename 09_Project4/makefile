JFLAGS = -cp "./lib/*:."
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        project3/ProcCpuMem.java \
        project3/Consumer.java \
        project3/Monitor.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) project3/*.class
