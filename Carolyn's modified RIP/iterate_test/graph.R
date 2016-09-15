library(ggplot2)
args <- commandArgs(TRUE)
srcFile <- args[1]
name = paste(srcFile, ".csv", sep="")
data <- read.csv(name, header=TRUE, nrows=75)
data <- data[data[["syllable"]] == "[CV]" | data[["syllable"]] =="[CVC]" | data[["syllable"]] =="[V]" | data[["syllable"]] =="[VC]" | data[["syllable"]] =="[CVC]" | data[["syllable"]] =="[CCV]" | data[["syllable"]] =="[VCC]" | data[["syllable"]] =="[CCVC]" | data[["syllable"]] =="[CVCC]" | data[["syllable"]] =="[CCVCC]", ]
ggplot(data,aes(iterations,accuracy,color=syllable))+geom_line()+labs(x="Iterations",y="Percent accurate")+ggtitle(srcFile)
graphname = paste(srcFile, "pdf", sep=".")
ggsave(file=graphname)