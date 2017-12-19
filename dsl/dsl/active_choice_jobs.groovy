import jenkins.model.*;
import hudson.model.*
def list =[]
Jenkins.instance.getAllItems(AbstractProject.class).each {it ->
  if (it.fullName.matches('EPBYMINW2033\\/MNTLAB-ilakhtenkov-child(.+)')) {
	list << "${it.name}:selected"
  }
}
return list
