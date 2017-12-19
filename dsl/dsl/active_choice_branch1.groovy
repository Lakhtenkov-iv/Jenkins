def getbranches = ("git ls-remote -t -h https://github.com/MNT-Lab/mntlab-dsl.git").execute()
return getbranches.text.readLines()
        .collect { it.split()[1].replaceAll('refs/heads/', '')  }
        .unique()
        .findAll { it.matches('ilakhtenkov|master' ) }
