
import styles from './Form.module.css'
import PropTypes from 'prop-types'
import Button from '../../../shared/components/Button'

function Form({ style, nextStepBtn = false, previousStepBtn = false, fields = [], formData = {}, onChange, enableDaysOfWeek = false, setFormData, onSubmit, enableBtnGroup = false}) {
    return (
        <form className={`${styles['form']}`} style={style} onSubmit={onSubmit}>
            {enableDaysOfWeek && (
            <div className='form-group' style={{order: -1}}>
                <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '10px'}}>
                <label><input type="checkbox" name="recurring" checked={formData.recurring || false} onChange={(e) => setFormData({...formData, recurring: e.target.checked})} />
                {' '}Recurring </label>
                    <label><input type="checkbox" name="indefinetely" checked={formData.indefinetely || false} onChange={(e) => setFormData({...formData, indefinetely: e.target.checked})} /> Run Indefinetely</label>
                    <label><input type="checkbox" name="immediately" checked={formData.immediately || false} onChange={(e) => setFormData({...formData, immediately: e.target.checked})} />Run Immediately</label>
                    <label><input type="checkbox" name="emailNotifications" checked={formData.emailNotifications || false} onChange={(e) => setFormData({...formData, emailNotifications: e.target.checked})} />Email Notifications</label>
                </div>
            </div>
          )}

            {fields.map((field, index) => {
                // Auto-detect password fields based on label
                const labelLower = field.label.toLowerCase();
                const isPasswordField = labelLower.includes('password') || labelLower.includes('access token');
                const inputType = field.type || (isPasswordField ? 'password' : 'text');
                
                // Hide fields based on checkbox states (keep in DOM to prevent layout shift)
                const isStopDateHidden = field.name === 'stopDate' && formData.indefinetely;
                const isStartDateHidden = field.name === 'startDate' && formData.immediately;
                const isEmailHidden = field.name === 'email' && !formData.emailNotifications;
                const isFieldHidden = isStopDateHidden || isStartDateHidden || isEmailHidden;
                
                return (
                    <div 
                        key={field.name || index} 
                        className={styles['form-group']}
                        style={isFieldHidden ? {
                            visibility: 'hidden',
                            height: 0,
                            margin: 0,
                            padding: 0,
                            overflow: 'hidden'
                        } : {}}
                    >
                        <label 
                            htmlFor={field.name} 
                            className={styles['form-group__label']}
                        >
                            {field.label}:
                        </label>
                        <input 
                            id={field.name}
                            name={field.name}
                            type={inputType}
                            value={formData[field.name] || ''}
                            onChange={onChange}
                            required={!isFieldHidden && field.required}
                            className={styles['form-group__input']}
                            disabled={isFieldHidden}
                        />
                    </div>
                );
            })}


        {enableDaysOfWeek && (
            <div className='form-group'>
                <label className='form-group__label'>

                   {formData.recurring && <label style={{ marginRight: '15px'}}><input type="radio" checked={formData.daily || false} onChange={() => setFormData({...formData, daily: true, monthly: false, yearly: false})} name="frequency" value="daily" /> Daily</label>}
                   {formData.recurring && <label style={{ marginRight: '15px'}}><input type="radio" checked={formData.monthly || false} onChange={() => setFormData({...formData, daily: false, monthly: true, yearly: false})} name="frequency" value="monthly" /> Monthly</label>}
                   {formData.recurring && <label style={{ marginRight: '15px'}}><input type="radio" checked={formData.yearly || false} onChange={() => setFormData({...formData, daily: false, monthly: false, yearly: true})} name="frequency" value="yearly" /> Yearly</label>}
                </label>
         
              {formData.daily && formData.recurring && (
                <>
                  <label className='form-group__label'>Days of Week:</label>
                  <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap'}}>
                    {['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'].map(day => (
                      <label key={day} style={{display: 'flex', alignItems: 'center', gap: '5px'}}>
                        <input 
                          type="checkbox" 
                          name={day} 
                          checked={formData[day] || false} 
                          onChange={(e) => setFormData({...formData, [day]: e.target.checked})}
                        />
                        {day.charAt(0).toUpperCase() + day.slice(1)}
                      </label>
                    ))}
                  </div>
                </>
              )}


              {formData.monthly && formData.recurring && (
                
                <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap', marginTop: '20px'}}>
                  <label className='form-group__label'>Day </label>
                  <input type="number" name="month" value={formData.month || ''} onChange={(e) => setFormData({...formData, month: e.target.value})} />
                  <label className='form-group__label'>of every </label>
                  <input type="number" name="month" value={formData.month || ''} onChange={(e) => setFormData({...formData, month: e.target.value})} />
                  <label className='form-group__label'>month(s)</label>
                </div>
              )}

              {formData.yearly && formData.recurring && (
                <div style={{marginTop: '20px'}}>
                  <label className='form-group__label'>Run once a year {formData.indefinetely ? `on ${formData.startDate}` : `from ${formData.startDate} - ${formData.stopDate}`} </label>
                </div>
              )}
            </div>
          )}

          {enableBtnGroup && (
            <div className={styles['form__button-group']}>
            {previousStepBtn && <Button onClick={previousStepBtn}>Back</Button>}
            {nextStepBtn && <Button onClick={nextStepBtn}>Next</Button>}
            <Button onClick={() => onSubmit(formData)}>Save</Button>
            </div>
          )}

          {!enableBtnGroup && (
            <Button className={styles['form__button--save']} onClick={() => onSubmit(formData)}>Save</Button>
          )}
        </form>
    )
}

Form.propTypes = {
    children: PropTypes.node,
    style: PropTypes.object,
    nextStepBtn: PropTypes.func,
    previousStepBtn: PropTypes.func,
    onSubmit: PropTypes.func,
    fields: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
        type: PropTypes.string,
        required: PropTypes.bool
    })),
    formData: PropTypes.object,
    onChange: PropTypes.func,
    enableDaysOfWeek: PropTypes.bool,
    setFormData: PropTypes.func,
    enableBtnGroup: PropTypes.bool
}

export default Form
