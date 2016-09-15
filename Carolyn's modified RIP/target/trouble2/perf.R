args <- commandArgs(TRUE)
its <- as.numeric(args[1]) #how many files are there
name <- args[2] #file name minus index
counts <- list()
for (f in seq(1,its)) #iterate through files
 {
    file = paste(paste(f,name,sep="-"),"csv",sep=".") #create file name
    data <- read.csv(file, header=TRUE) #read in file
    data <- data[data[["syllable"]] == "[CV]" | data[["syllable"]] =="[CVC]" | data[["syllable"]] =="[V]" | data[["syllable"]] =="[VC]" | data[["syllable"]] =="[CVC]" | data[["syllable"]] =="[CCV]" | data[["syllable"]] =="[VCC]" | data[["syllable"]] =="[CCVC]" | data[["syllable"]] =="[CVCC]" | data[["syllable"]] =="[CCVCC]", ]
    syll_acc <- list() #List of earliest accurate production of each syllable (list of (iteration,syllable) pairs)
    for (syll in data[1:9,][["syllable"]]){ #Iterate through syllables
    	data_acc <- data[data[["syllable"]] == syll & data[["iterations"]] == 1000, ] #filter by syllable and by accurate iterations
    	if(as.numeric(data_acc[["accuracy"]]) > 0.80) {
            if(syll %in% names(counts)){
                counts[syll] <- as.numeric(counts[syll]) + 1
            }
            else{
                counts[syll] = 1
            }
        }
    }
 }
for (n in names(counts)) {
	print(paste(paste("Syllable:", n),paste(paste("learned in",counts[[n]]/its*100), "% of times.")))
}