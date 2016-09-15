library(ggplot2)
args <- commandArgs(TRUE)
srcFile <- args[1]
name = paste(srcFile, ".csv", sep="")
data <- read.csv(name)
ggplot(data,aes(iterations,accuracy,color=syllable))+geom_line()+labs(x="Iterations",y="Percent accurate")+ggtitle(srcFile)
graphname = paste(srcFile, ".pdf", sep="")
ggsave(file=graphname)